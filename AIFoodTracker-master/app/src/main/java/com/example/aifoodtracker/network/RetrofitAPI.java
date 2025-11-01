package com.example.aifoodtracker.network;

import com.example.aifoodtracker.domain.FoodResponse;
import com.example.aifoodtracker.domain.WalkResponse;

import java.util.List; // ⭐️ List import 추가

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET; // ⭐️ GET import 추가
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query; // ⭐️ Query import 추가

public interface RetrofitAPI {

    // 1. AI 음식 이미지 분석 (기존 코드)
    @Multipart
    @POST("/v1/images")
    Call<FoodResponse> uploadImage(@Part MultipartBody.Part file);

    // 2. BMI 계산 (기존 코드 - 사용 안 함)
    @POST("/v1/bmi")
    Call<Void> calculateBmi(@Body Object user);

    // 3. 산책길 추천 (기존 코드)
    @GET("/v1/walks")
    Call<WalkResponse> getWalks(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("calorie") double calorie
    );

    // ⭐️ 4. (신규) 음식 이름 검색 API ⭐️
    @GET("/v1/search_food")
    Call<List<FoodResponse>> searchFoodByName(
            @Query("name") String foodName
    );
}
