package com.example.aifoodtracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aifoodtracker.domain.User;
import com.google.gson.Gson;

public class UserPreferenceManager {

    private static final String PREF_NAME = "user_pref";
    private static final String KEY_USER_DATA = "user_data";

    // User 저장
    public static void saveUser(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(KEY_USER_DATA, json);
        editor.apply();
    }

    // User 불러오기
    public static User getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_USER_DATA, null);
        return gson.fromJson(json, User.class);
    }

    // User 삭제
    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_DATA).apply();
    }
}
