package com.example.aifoodtracker;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.aifoodtracker.domain.WalkResponse;
import com.example.aifoodtracker.domain.WalkRoute;
import com.example.aifoodtracker.network.RetrofitAPI;
import com.example.aifoodtracker.network.RetrofitClient;

// 🚨 아래 import 줄들 맨 앞에 // 를 붙여주세요 (이미 주석 처리 되어 있어야 함)
// import com.naver.maps.geometry.LatLng;
// import com.naver.maps.map.CameraUpdate;
// import com.naver.maps.map.LocationTrackingMode;
// import com.naver.maps.map.MapFragment;
// import com.naver.maps.map.NaverMap;
// import com.naver.maps.map.OnMapReadyCallback; // 🚨 이 줄도 주석 처리!
// import com.naver.maps.map.overlay.Marker;
// import com.naver.maps.map.overlay.OverlayImage;
// import com.naver.maps.map.util.FusedLocationSource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 🚨 OnMapReadyCallback 인터페이스 구현 부분을 제거합니다!
public class MapActivity extends FragmentActivity { // OnMapReadyCallback 제거됨

    // 🚨 NaverMap 관련 변수들 주석 처리 (이미 주석 처리 되어 있어야 함)
    // private NaverMap naverMap;
    // private FusedLocationSource locationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private double foodCalories = 580.0; // AI 분석 결과에서 전달받을 값 (이건 유지)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 🚨 네이버 지도 초기화 코드 전체 주석 처리 (이미 주석 처리 되어 있어야 함)
        /*
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        */

        // 지도 기능이 없으므로 사용자에게 안내 메시지 표시 (임시)
        Toast.makeText(this, "지도 기능을 임시로 비활성화했습니다.", Toast.LENGTH_LONG).show();
    }

    // 🚨 onMapReady 메소드 전체 주석 처리 (이미 주석 처리 되어 있어야 함)
    /*
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // ... (내용 생략) ...
    }
    */

    // 🚨 setupLocationListener 메소드 전체 주석 처리 (이미 주석 처리 되어 있어야 함)
    /*
    private void setupLocationListener() {
        // ... (내용 생략) ...
    }
    */

    // 산책길 정보 로드 (이 메소드는 유지하되, 지도에 마커 찍는 부분은 주석 처리 - 이미 되어 있어야 함)
    private void loadWalkRoutes(double lat, double lng, double calories) {
        RetrofitAPI api = RetrofitClient.getApiService();
        Call<WalkResponse> call = api.getWalks(lat, lng, calories);

        call.enqueue(new Callback<WalkResponse>() {
            @Override
            public void onResponse(Call<WalkResponse> call, Response<WalkResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ... (기존 로그/Toast 코드) ...

                    runOnUiThread(() -> {
                        // 🚨 지도 관련 코드 (마커 추가) 주석 처리 (이미 되어 있어야 함)
                        /*
                        // ... (마커 추가 코드) ...
                        */
                        Toast.makeText(MapActivity.this, "산책길 데이터 로드 성공 (마커 표시는 비활성화됨)", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(MapActivity.this, "서버 응답 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WalkResponse> call, Throwable t) {
                Toast.makeText(MapActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🚨 onRequestPermissionsResult 메소드 전체 주석 처리 (이미 되어 있어야 함)
    /*
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
       // ... (내용 생략) ...
    }
    */
}

