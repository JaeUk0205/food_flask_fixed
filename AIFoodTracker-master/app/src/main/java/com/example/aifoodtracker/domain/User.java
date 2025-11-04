package com.example.aifoodtracker.domain;

import java.io.Serializable;

// ❗️ 중요: Intent로 넘기지 않고 SharedPreferences에 저장할 것이므로
// Parcelable 대신 Serializable을 구현 (Gson이 사용)
public class User implements Serializable {

    // 기존 필드
    private String id;
    private String gender;
    private double height;
    private double weight;
    private int targetCalories;

    // ⭐️ 새로 추가된 필드 ⭐️
    private double bloodPressureSys; // 수축기 혈압
    private double bloodPressureDia; // 이완기 혈압
    private double bloodSugar;       // 공복 혈당

    // --- Getter 메소드들 ---
    public String getId() { return id; }
    public String getGender() { return gender; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public int getTargetCalories() { return targetCalories; }
    public double getBloodPressureSys() { return bloodPressureSys; }
    public double getBloodPressureDia() { return bloodPressureDia; }
    public double getBloodSugar() { return bloodSugar; }

    // --- Setter 메소드들 ---
    public void setId(String id) { this.id = id; }
    public void setGender(String gender) { this.gender = gender; }
    public void setHeight(double height) { this.height = height; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }

    //혈압/혈당 Setter
    public void setBloodPressure(double sys, double dia) {
        this.bloodPressureSys = sys;
        this.bloodPressureDia = dia;
    }

    public void setBloodSugar(double sugar) {
        this.bloodSugar = sugar;
    }
}

