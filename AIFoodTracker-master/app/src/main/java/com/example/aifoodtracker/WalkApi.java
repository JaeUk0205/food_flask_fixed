package com.example.aifoodtracker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// ✅ 반드시 'interface' 로 생성!
public interface WalkApi {

    // Flask의 /v1/walkspots 호출 → 산책 장소 리스트 받기
    @GET("/v1/walkspots")
    Call<List<WalkSpot>> getWalkspots(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radius") int radius,
            @Query("limit") int limit
    );

    // Flask의 /v1/route 호출 → 경로(polyline) 받기
    @GET("/v1/route")
    Call<RouteResponse> getRoute(
            @Query("start_lat") double startLat,
            @Query("start_lng") double startLng,
            @Query("end_lat") double endLat,
            @Query("end_lng") double endLng,
            @Query("mode") String mode
    );
}
