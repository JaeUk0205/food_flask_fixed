package com.example.aifoodtracker.network;

import com.example.aifoodtracker.domain.FoodResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitAPI {
    @Multipart
    @POST("/v1/images")
    Call<FoodResponse> uploadImage(@Part MultipartBody.Part file);
}