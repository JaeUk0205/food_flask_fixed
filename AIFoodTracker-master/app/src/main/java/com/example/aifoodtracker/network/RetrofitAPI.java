package com.example.aifoodtracker.network;

import com.example.aifoodtracker.domain.FoodResponse;
import com.example.aifoodtracker.domain.WalkResponse;   // ✅ WalkResponse import 추가

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;                              // ✅ Retrofit GET 추가
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;                            // ✅ Retrofit Query 추가

public interface RetrofitAPI {

    // 🔹 음식 이미지 업로드 (AI 분석)
    @Multipart
    @POST("/v1/images")
    Call<FoodResponse> uploadImage(@Part MultipartBody.Part file);

    // 🔹 산책길 추천 (현재 위치 + 칼로리 기반)
    @GET("/v1/walks")
    Call<WalkResponse> getWalks(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("calorie") double calorie
    );
}
