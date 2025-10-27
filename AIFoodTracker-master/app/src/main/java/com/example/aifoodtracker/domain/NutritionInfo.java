package com.example.aifoodtracker.domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class NutritionInfo implements Serializable {
    @SerializedName("calories")
    private float calories;

    @SerializedName("carbohydrate")
    private float carbohydrate;

    @SerializedName("protein")
    private float protein;

    @SerializedName("fat")
    private float fat;

    @SerializedName("sugar")
    private float sugar;

    // Getters...
    public float getCalories() { return calories; }
    public float getCarbohydrate() { return carbohydrate; }
    public float getProtein() { return protein; }
    public float getFat() { return fat; }
    public float getSugar() { return sugar; }
}