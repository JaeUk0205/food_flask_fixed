package com.example.aifoodtracker;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import com.example.aifoodtracker.domain.WalkResponse;
import com.example.aifoodtracker.domain.WalkRoute;
import com.example.aifoodtracker.network.RetrofitAPI;
import com.example.aifoodtracker.network.RetrofitClient;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment; // 구글의 SupportMapFragment가 아님
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource; // 구글의 FusedLocationProviderClient가 아님

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private NaverMap mNaverMap;
    private FusedLocationSource mLocationSource;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private double foodCalories = 580.0; // TODO: AI 분석 결과에서 전달받아야 함

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // FusedLocationSource 객체 생성
        mLocationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // SupportMapFragment 가 아닌 MapFragment 를 사용
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        mNaverMap = naverMap;

        // 위치 권한 확인 및 현재 위치 활성화
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };

        if (ActivityCompat.checkSelfPermission(this, permissions[0]) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, permissions[1]) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 이미 있을 경우, 위치 추적 시작
            mNaverMap.setLocationSource(mLocationSource);
            mNaverMap.setLocationTrackingMode(com.naver.maps.map.LocationTrackingMode.Follow);

            // FusedLocationSource를 사용하여 현재 위치 가져오기 (1회성)
            mLocationSource.activate(location -> {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    loadWalkRoutes(lat, lng, foodCalories);
                    mNaverMap.moveCamera(CameraUpdate.scrollTo(new LatLng(lat, lng)));
                    mLocationSource.deactivate(); // 1회성 위치 업데이트 후 비활성화
                }
            });
        }
    }

    private void loadWalkRoutes(double lat, double lng, double calories) {
        RetrofitAPI api = RetrofitClient.getApiService();
        Call<WalkResponse> call = api.getWalks(lat, lng, calories);

        call.enqueue(new Callback<WalkResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalkResponse> call, @NonNull Response<WalkResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WalkResponse data = response.body();
                    List<WalkRoute> nearby = data.getNearbyRoutes();
                    List<WalkRoute> famous = data.getFamousRoutes();

                    runOnUiThread(() -> {
                        if (nearby != null) {
                            for (WalkRoute route : nearby) {
                                Marker marker = new Marker();
                                marker.setPosition(new LatLng(route.getLat(), route.getLng()));
                                marker.setCaptionText(route.getName());
                                marker.setSubCaptionText("거리: " + route.getDistance() + " km (근처 코스)");
                                marker.setMap(mNaverMap);
                            }
                        }

                        if (famous != null) {
                            for (WalkRoute route : famous) {
                                Marker marker = new Marker();
                                marker.setPosition(new LatLng(route.getLat(), route.getLng()));
                                marker.setCaptionText("⭐ " + route.getName());
                                marker.setSubCaptionText("도시: " + route.getCity() + " / 거리: " + route.getDistance() + " km");
                                marker.setMap(mNaverMap);
                            }
                        }
                    });
                } else {
                    Toast.makeText(MapActivity.this, "서버 응답 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WalkResponse> call, @NonNull Throwable t) {
                Toast.makeText(MapActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (mLocationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (mLocationSource.isActivated()) { // 권한이 부여되었는지 확인
                mNaverMap.setLocationTrackingMode(com.naver.maps.map.LocationTrackingMode.Follow);
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

