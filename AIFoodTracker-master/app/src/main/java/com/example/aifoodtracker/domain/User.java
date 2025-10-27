package com.example.aifoodtracker.domain;

import android.os.Parcel;
import android.os.Parcelable; // Correct import
import androidx.annotation.NonNull;

// The interface name was misspelled. It should be 'Parcelable'.
public class User implements Parcelable {

    private String id;
    private String gender;
    private double height;
    private double weight;
    private int targetCalories;

    public User() {
    }

    // --- Getters and Setters for all variables ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }


    // --- Parcelable implementation code ---
    protected User(Parcel in) {
        id = in.readString();
        gender = in.readString();
        height = in.readDouble();
        weight = in.readDouble();
        targetCalories = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(gender);
        dest.writeDouble(height);
        dest.writeDouble(weight);
        dest.writeInt(targetCalories);
    }
}