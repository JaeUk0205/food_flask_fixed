package com.example.aifoodtracker.domain;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RiskInfo implements Serializable {
    @SerializedName("calories")
    private String calories;

    @SerializedName("sugar")
    private String sugar;

    // Getters...
    public String getCalories() { return calories; }
    public String getSugar() { return sugar; }
}