import torch
import torch.nn as nn
from torchvision import models, transforms
from PIL import Image
import pandas as pd
import os

print("--- Classifier.py vFINAL_ROSTER_MAP 실행 중 ---")

# -------------------------------------------------------------------
# 1. 클래스 이름 목록('출석부') 생성 (class_map.csv 사용)
# -------------------------------------------------------------------
CLASS_NAMES = [] # '학번'이 필요 없으므로, 순서만 있는 리스트(출석부)를 사용합니다.
csv_filename = 'class_map.csv'
csv_path = os.path.join(os.path.dirname(__file__), csv_filename)

try:
    # pandas로 CSV 파일을 읽습니다.
    # 'header=0'을 통해 첫 줄을 헤더로 인식하고, 실제 데이터는 두 번째 줄부터 읽습니다.
    df = pd.read_csv(csv_path, header=0) 
    
    # --- ✅ 여기가 최종 수정된 핵심 부분입니다 ---
    # 두 번째 열('name' 열)에 있는 이름만 순서대로 가져와 리스트(출석부)를 만듭니다.
    CLASS_NAMES = df.iloc[:, 1].str.strip().tolist()
            
    print(f"✅ 클래스 이름 목록({len(CLASS_NAMES)}개)을 성공적으로 불러왔습니다.")

except FileNotFoundError:
    print(f"❌ 오류: '{csv_filename}' 파일을 찾을 수 없습니다.")
    CLASS_NAMES = []
except Exception as e:
    print(f"❌ 오류: 클래스 목록 파일 처리 중 문제 발생: {e}")
    CLASS_NAMES = []

# -------------------------------------------------------------------
# 2. 모델 구조 정의 (EfficientNet-B0, 372 클래스)
# -------------------------------------------------------------------
model = models.efficientnet_b0(weights=None)
num_classes = 372
model.classifier[1] = nn.Linear(model.classifier[1].in_features, num_classes)

# -------------------------------------------------------------------
# 3. 모델 가중치 로드
# -------------------------------------------------------------------
try:
    state_dict = torch.load('best_model.pt', map_location='cpu')
    model.load_state_dict(state_dict)
    model.eval()
    print("✅ 모델과 가중치가 성공적으로 로드되었습니다.")
except Exception as e:
    print(f"❌ 모델 로딩 중 오류 발생: {e}")
    model = None

# -------------------------------------------------------------------
# 4. 이미지 전처리 설정
# -------------------------------------------------------------------
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])

# -------------------------------------------------------------------
# 5. 분석 함수
# -------------------------------------------------------------------
def classify_image(image_path: str):
    if model is None or not CLASS_NAMES:
        return "분석 실패 (초기화 오류)", 0.0
    try:
        image = Image.open(image_path).convert('RGB')
        image_tensor = transform(image).unsqueeze(0)
        with torch.no_grad():
            outputs = model(image_tensor)
            probabilities = torch.nn.functional.softmax(outputs, dim=1)
            confidence, predicted_idx_tensor = torch.max(probabilities, 1)
            predicted_idx = predicted_idx_tensor.item()

            # AI가 알려준 '순서(인덱스)'로 출석부(CLASS_NAMES)에서 이름을 찾습니다.
            if 0 <= predicted_idx < len(CLASS_NAMES):
                label = CLASS_NAMES[predicted_idx]
            else:
                label = f"알 수 없는 음식_{predicted_idx}"
            
            return label, confidence.item()
    except Exception as e:
        print(f"❌ 이미지 분석 중 오류 발생: {e}")
        return "분석 중 오류", 0.0