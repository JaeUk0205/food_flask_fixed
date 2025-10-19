from flask_sqlalchemy import SQLAlchemy
import unicodedata # ◀◀ 1. 이 줄을 추가합니다.
import re

db = SQLAlchemy()

class Prediction(db.Model):
    id = db.Column(db.String, primary_key=True)
    image_path = db.Column(db.String, nullable=False)
    label = db.Column(db.String, nullable=False)
    confidence = db.Column(db.Float, nullable=False)

def normalize_key(text: str):
    # --- ✅ 여기가 수정/강화되었습니다 ---
    # 1. NFC 방식으로 한글 조합을 표준화합니다.
    normalized_text = unicodedata.normalize('NFC', text)
    # 2. 소문자로 바꾸고, 괄호와 그 안의 내용을 제거하고, 공백을 제거합니다.
    s = normalized_text.lower().strip()
    s = re.sub(r'\([^)]*\)', '', s)
    s = re.sub(r'\s+', '', s)
    return s

class Nutrition(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String, nullable=False)
    name_key = db.Column(db.String, unique=True, nullable=False, index=True)
    kcal = db.Column(db.String)
    carbs_g = db.Column(db.String)
    protein_g = db.Column(db.String)
    fat_g = db.Column(db.String)
    sugar_g = db.Column(db.String)
    sodium_mg = db.Column(db.String)