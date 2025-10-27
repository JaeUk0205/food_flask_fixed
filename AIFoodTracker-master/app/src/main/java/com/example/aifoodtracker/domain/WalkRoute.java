package com.example.aifoodtracker.domain;

import com.google.gson.annotations.SerializedName;

public class WalkRoute {

    @SerializedName("name")
    private String name;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    @SerializedName("distance")
    private double distance;

    // ✅ 새로 추가
    @SerializedName("city")
    private String city;

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getDistance() {
        return distance;
    }

    // ✅ 새로 추가된 getter
    public String getCity() {
        return city != null ? city : "";
    }

    // ✅ (선택) setter 추가 - 혹시 필요할 경우
    public void setCity(String city) {
        this.city = city;
    }
}
