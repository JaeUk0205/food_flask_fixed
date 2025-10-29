package com.example.aifoodtracker.domain;

import android.icu.text.SimpleDateFormat; // 날짜 포맷팅용 import 추가

import java.io.Serializable;
import java.util.Date; // Date import 추가
import java.util.Locale; // Locale import 추가

// RecyclerView 항목 하나에 표시될 데이터 정의
public class FoodEntry implements Serializable {
    private String foodName;
    private String imageUri; // 이미지 경로 (String으로 저장)
    private NutritionInfo nutritionInfo;
    private long timestamp; // 기록 시간 (long 타입으로 저장)

    // 생성자: 필수 정보 받아서 객체 생성
    public FoodEntry(String foodName, String imageUri, NutritionInfo nutritionInfo) {
        this.foodName = foodName;
        this.imageUri = imageUri;
        this.nutritionInfo = nutritionInfo;
        this.timestamp = System.currentTimeMillis(); // 현재 시간 기록
    }

    // Getter 메소드들
    public String getFoodName() {
        return foodName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public NutritionInfo getNutritionInfo() {
        return nutritionInfo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // 시간을 "오후 3:15" 같은 형식으로 변환해서 반환하는 메소드 (Adapter에서 사용)
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREA); // 예: 오후 3:15
        return sdf.format(new Date(timestamp));
    }

    // 영양 정보 요약 텍스트 반환 (Adapter에서 사용)
    public String getNutritionSummary() {
        if (nutritionInfo == null) {
            return "영양 정보 없음";
        }
        return String.format(Locale.KOREA, "칼로리: %.1f kcal, 탄: %.1fg, 단: %.1fg, 지: %.1fg",
                nutritionInfo.getCalories(),
                nutritionInfo.getCarbohydrate(),
                nutritionInfo.getProtein(),
                nutritionInfo.getFat());
    }
}
// 🚨 이 아래에 아무 코드도 없어야 합니다! (No code should be below this line!)

