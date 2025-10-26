# app.py (AI 음식 분석 + BMI + 산책길 추천 통합 버전)
import os
import ssl
from uuid import uuid4
from math import radians, cos, sin, asin, sqrt
from flask import Flask, request, jsonify, send_from_directory, url_for
from werkzeug.utils import secure_filename
from flask_cors import CORS

ssl._create_default_httpserver_context = ssl._create_unverified_context

from classifier import classify_image
from database import db, Nutrition, normalize_key

RISK_THRESHOLDS = {"calories": 600, "sugar": 20}

BASE_DIR = os.path.abspath(os.path.dirname(__file__))
UPLOAD_DIR = os.path.join(BASE_DIR, "uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)
ALLOWED_MIME = {"image/jpeg", "image/png", "image/webp"}
MAX_MB = 10
ALIASES = {"bibimbap": "비빔밥", "kimchi": "김치", "ramen": "라면"}

app = Flask(__name__)
CORS(app)
app.config['JSON_AS_ASCII'] = False


app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///" + os.path.join(BASE_DIR, "food_nutrition.db")
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
app.config["MAX_CONTENT_LENGTH"] = MAX_MB * 1024 * 1024
db.init_app(app)

# --------------------------
# 업로드된 이미지 파일 제공
# --------------------------
@app.route("/uploads/<path:filename>")
def uploaded_file(filename):
    return send_from_directory(UPLOAD_DIR, filename)


# --------------------------
# 1️⃣ AI 음식 이미지 분석
# --------------------------
@app.route("/v1/images", methods=["POST"])
def api_upload_image():
    if "file" not in request.files:
        return jsonify({"error": "file field required"}), 400

    f = request.files["file"]
    if not (f and f.filename and f.mimetype in ALLOWED_MIME):
        return jsonify({"error": "Invalid file or file type"}), 400

    try:
        filename = f"{uuid4()}{os.path.splitext(secure_filename(f.filename))[1].lower() or '.jpg'}"
        save_path = os.path.join(UPLOAD_DIR, filename)
        f.save(save_path)
        label, conf = classify_image(save_path)
        std_label = ALIASES.get(str(label).strip().lower(), label)
    except Exception as e:
        return jsonify({"error": f"Failed to process image: {e}"}), 500

    nut = Nutrition.query.filter_by(name_key=normalize_key(std_label)).first()
    nutrition_info = {
        "calories": float(nut.kcal) if nut and nut.kcal is not None else 0.0,
        "carbohydrate": float(nut.carbs_g) if nut and nut.carbs_g is not None else 0.0,
        "protein": float(nut.protein_g) if nut and nut.protein_g is not None else 0.0,
        "fat": float(nut.fat_g) if nut and nut.fat_g is not None else 0.0,
        "sugar": float(nut.sugar_g) if nut and nut.sugar_g is not None else 0.0,
    } if nut else None

    risk_info = None
    if nutrition_info:
        risk_info = {
            "calories": "high" if nutrition_info["calories"] > RISK_THRESHOLDS["calories"] else "normal",
            "sugar": "high" if nutrition_info["sugar"] > RISK_THRESHOLDS["sugar"] else "normal",
        }

    response_data = {
        "foodName": std_label,
        "confidence": float(conf),
        "imageUrl": url_for('uploaded_file', filename=filename, _external=True),
        "nutritionInfo": nutrition_info,
        "riskInfo": risk_info
    }
    return jsonify(response_data), 200


# --------------------------
# 2️⃣ BMI 계산
# --------------------------
@app.route("/v1/bmi", methods=["POST"])
def calculate_bmi():
    data = request.get_json()
    if not data or "height" not in data or "weight" not in data or "gender" not in data:
        return jsonify({"error": "height, weight, and gender are required"}), 400
    try:
        height_cm = float(data["height"])
        weight_kg = float(data["weight"])
        gender = data["gender"]
        if gender not in ["male", "female"]:
            return jsonify({"error": "gender must be 'male' or 'female'"}), 400
        if height_cm <= 0 or weight_kg <= 0:
            return jsonify({"error": "height and weight must be positive values"}), 400
        height_m = height_cm / 100
        bmi = weight_kg / (height_m ** 2)
        if bmi < 18.5: status = "저체중"
        elif 18.5 <= bmi < 23: status = "정상 체중"
        elif 23 <= bmi < 25: status = "과체중"
        else: status = "비만"
        gender_text = "남성" if gender == "male" else "여성"
        message = f"{gender_text} 기준, {status}입니다."
        response_data = {"bmi": round(bmi, 2), "status": status, "message": message}
        return jsonify(response_data), 200
    except (ValueError, TypeError):
        return jsonify({"error": "Invalid data type for height or weight"}), 400


# --------------------------
# 3️⃣ 산책길 추천 (현재 위치 + 칼로리 기반)
# --------------------------
def haversine(lat1, lon1, lat2, lon2):
    R = 6371
    d_lat = radians(lat2 - lat1)
    d_lon = radians(lon2 - lon1)
    a = sin(d_lat/2)**2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(d_lon/2)**2
    return R * 2 * asin(sqrt(a))

@app.route("/v1/walks", methods=["GET"])
def recommend_walks():
    try:
        user_lat = float(request.args.get("lat", 0))
        user_lng = float(request.args.get("lng", 0))
        calorie = float(request.args.get("calorie", 0))  # 음식 칼로리
    except (ValueError, TypeError):
        return jsonify({"error": "Invalid coordinates or calorie"}), 400

    # 목표 거리(km) = 칼로리 / 65 (성인 평균 1km당 65kcal)
    target_distance = round(calorie / 65, 2) if calorie > 0 else 3.0

    # 내 주변 산책코스 (샘플)
    nearby_routes = [
        {"name": "수원대 캠퍼스길", "lat": 37.2091, "lng": 126.9762},
        {"name": "봉담 호수공원", "lat": 37.2134, "lng": 126.9835},
        {"name": "탑동 시민공원", "lat": 37.2085, "lng": 126.9710},
        {"name": "수원천길", "lat": 37.2860, "lng": 127.0176}
    ]

    for route in nearby_routes:
        route["distance"] = round(haversine(user_lat, user_lng, route["lat"], route["lng"]), 2)
    nearby_routes = sorted(nearby_routes, key=lambda x: x["distance"])[:5]

    # 유명 산책코스
    famous_routes = [
        {"name": "남산둘레길", "city": "서울", "distance": 7.5},
        {"name": "한강공원 코스", "city": "서울", "distance": 5.8},
        {"name": "수원천 산책길", "city": "수원", "distance": 8.2},
        {"name": "해운대 해변길", "city": "부산", "distance": 6.4}
    ]

    result = {
        "targetDistance": target_distance,
        "nearbyRoutes": nearby_routes,
        "famousRoutes": famous_routes
    }

    return jsonify(result), 200


# --------------------------
# 서버 실행
# --------------------------
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000, debug=True)

