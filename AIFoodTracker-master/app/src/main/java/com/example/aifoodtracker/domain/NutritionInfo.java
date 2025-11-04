package com.example.aifoodtracker.domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class NutritionInfo implements Serializable {

    // 서버 응답 필드 (예시)
    @SerializedName("calories")
    private double calories; // 칼로리
    @SerializedName("carbohydrate")
    private double carbohydrate; // 탄수화물
    @SerializedName("protein")
    private double protein; // 단백질
    @SerializedName("fat")
    private double fat; // 지방
    @SerializedName("sugar")
    private double sugar; // 당 (있을 수도 없을 수도 있음)

    // 기본 생성자 (필요할 수 있음)
    public NutritionInfo() {}

    // --- Getter 메소드들 ---
    public double getCalories() {
        return calories;
    }
    public double getCarbohydrate() {
        return carbohydrate;
    }
    public double getProtein() {
        return protein;
    }
    public double getFat() {
        return fat;
    }
    public double getSugar() { return sugar; } // sugar getter (선택 사항)


    // Setter 메소드들 추가
    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setCarbohydrate(double carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public void setSugar(double sugar) { this.sugar = sugar; } // sugar setter (선택 사항)
}
