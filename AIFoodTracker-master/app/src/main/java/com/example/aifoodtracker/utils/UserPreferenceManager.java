package com.example.aifoodtracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aifoodtracker.domain.FoodEntry;
import com.example.aifoodtracker.domain.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class UserPreferenceManager {

    private static final String PREF_NAME = "AIFoodTrackerPrefs";
    private static final String KEY_USER = "user";

    private static final String KEY_ACCUMULATED_CALORIES = "accumulated_calories";
    private static final String KEY_ACCUMULATED_CARB = "accumulated_carb";
    private static final String KEY_ACCUMULATED_PROTEIN = "accumulated_protein";
    private static final String KEY_ACCUMULATED_FAT = "accumulated_fat";
    private static final String KEY_MEAL_LIST = "meal_list";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- User 저장/로드 ---
    public static void saveUser(Context context, User user) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(KEY_USER, json);
        editor.apply();
    }

    public static User getUser(Context context) {
        Gson gson = new Gson();
        String json = getPreferences(context).getString(KEY_USER, null);
        return gson.fromJson(json, User.class);
    }

    // --- 누적 값 저장 ---
    public static void saveAccumulatedValues(Context context, int calories, int carb, int protein, int fat) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_ACCUMULATED_CALORIES, calories);
        editor.putInt(KEY_ACCUMULATED_CARB, carb);
        editor.putInt(KEY_ACCUMULATED_PROTEIN, protein);
        editor.putInt(KEY_ACCUMULATED_FAT, fat);
        editor.apply();
    }

    // --- 누적 값 로드 ---
    public static int getAccumulatedCalories(Context context) {
        return getPreferences(context).getInt(KEY_ACCUMULATED_CALORIES, 0);
    }
    public static int getAccumulatedCarb(Context context) {
        return getPreferences(context).getInt(KEY_ACCUMULATED_CARB, 0);
    }
    public static int getAccumulatedProtein(Context context) {
        return getPreferences(context).getInt(KEY_ACCUMULATED_PROTEIN, 0);
    }
    public static int getAccumulatedFat(Context context) {
        return getPreferences(context).getInt(KEY_ACCUMULATED_FAT, 0);
    }

    // --- 식단 목록(mealList) 저장 ---
    public static void saveMealList(Context context, ArrayList<FoodEntry> mealList) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mealList);
        editor.putString(KEY_MEAL_LIST, json);
        editor.apply();
    }

    // --- 식단 목록(mealList) 로드 ---
    public static ArrayList<FoodEntry> getMealList(Context context) {
        Gson gson = new Gson();
        String json = getPreferences(context).getString(KEY_MEAL_LIST, null);
        Type type = new TypeToken<ArrayList<FoodEntry>>() {}.getType();
        ArrayList<FoodEntry> list = gson.fromJson(json, type);
        return (list != null) ? list : new ArrayList<>();
    }

    // ⭐️⭐️⭐️ 오늘 기록 초기화 (누적값 + 식단 목록) ⭐️⭐️⭐️
    public static void clearTodayData(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_ACCUMULATED_CALORIES, 0);
        editor.putInt(KEY_ACCUMULATED_CARB, 0);
        editor.putInt(KEY_ACCUMULATED_PROTEIN, 0);
        editor.putInt(KEY_ACCUMULATED_FAT, 0);
        editor.remove(KEY_MEAL_LIST); // 또는 editor.putString(KEY_MEAL_LIST, "[]");
        editor.apply();
    }

    // --- (선택) 모든 데이터 삭제 ---
    public static void clearAll(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.clear();
        editor.apply();
    }
}

