import csv
import os
import unicodedata
import re
from app import app
from database import db, Nutrition

def normalize_key(text: str):
    if not isinstance(text, str): return ""
    normalized_text = unicodedata.normalize('NFC', text)
    s = normalized_text.lower().strip()
    s = re.sub(r'\([^)]*\)', '', s)
    s = re.sub(r'\s+', '', s)
    return s

with app.app_context():
    db.drop_all()
    db.create_all()

    csv_path = os.path.join(os.path.dirname(__file__), "nutrition.csv")
    
    # --- ✅ 중복 키를 확인하기 위한 세트(set) 추가 ---
    existing_keys = set()

    with open(csv_path, "r", encoding="utf-8-sig") as f:
        reader = csv.reader(f)
        try:
            next(reader) # 헤더가 있다면 건너뛰기
        except StopIteration:
            pass # 파일이 비어있으면 무시

        for row in reader:
            if len(row) < 2:
                continue
            
            food_name = row[1].strip()
            key = normalize_key(food_name)

            # --- ✅ 중복 검사 로직 추가 ---
            # 이미 추가된 키라면 건너뛰기
            if not key or key in existing_keys:
                continue
            
            existing_keys.add(key)
            
            new_nutrition = Nutrition(
                name=food_name,
                name_key=key,
                kcal=row[2] if len(row) > 2 and row[2] else 0.0,
                carbs_g=row[3] if len(row) > 3 and row[3] else 0.0,
                protein_g=row[4] if len(row) > 4 and row[4] else 0.0,
                fat_g=row[5] if len(row) > 5 and row[5] else 0.0,
                sugar_g=row[6] if len(row) > 6 and row[6] else 0.0,
                sodium_mg=row[7] if len(row) > 7 and row[7] else 0.0,
            )
            db.session.add(new_nutrition)
    
    db.session.commit()

print("✅ (중복 처리 완료) 데이터베이스가 성공적으로 초기화되고 영양 정보가 입력되었습니다.")