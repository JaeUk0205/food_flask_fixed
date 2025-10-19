package com.example.aifoodtracker.domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class FoodResponse implements Serializable {
    @SerializedName("foodName")
    private String foodName;

    @SerializedName("confidence")
    private double confidence;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("nutritionInfo")
    private NutritionInfo nutritionInfo;

    @SerializedName("riskInfo")
    private RiskInfo riskInfo;

    // Getters
    public String getFoodName() { return foodName; }
    public double getConfidence() { return confidence; }
    public String getImageUrl() { return imageUrl; }
    public NutritionInfo getNutritionInfo() { return nutritionInfo; }
    public RiskInfo getRiskInfo() { return riskInfo; }
}