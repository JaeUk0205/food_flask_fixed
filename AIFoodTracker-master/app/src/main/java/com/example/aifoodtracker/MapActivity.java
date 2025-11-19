package com.example.aifoodtracker;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    // Retrofit API
    private WalkApi walkApi;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // 위치 서비스
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 지도 Fragment 불러오기
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // ✅ Retrofit 연결 설정
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.35.113:8000/") // 에뮬레이터 기준 Flask 주소
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        walkApi = retrofit.create(WalkApi.class);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();

        // ✅ 기본 위치 (수원대 근처)
        LatLng suwon = new LatLng(37.2091, 126.9762);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(suwon, 15));

        // 지도 준비되면 산책 장소 로드
        loadWalkSpots(suwon.latitude, suwon.longitude);
    }

    /** ✅ 주변 산책 장소 가져오기 */
    private void loadWalkSpots(double lat, double lng) {
        walkApi.getWalkspots(lat, lng, 1500, 10).enqueue(new Callback<List<WalkSpot>>() {
            @Override
            public void onResponse(Call<List<WalkSpot>> call, Response<List<WalkSpot>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(MapActivity.this, "서버 응답 오류", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<WalkSpot> spots = response.body();
                for (WalkSpot s : spots) {
                    LatLng pos = new LatLng(s.lat, s.lng);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(s.name)
                            .snippet(s.address + " ⭐" + s.rating));
                    marker.setTag(s);
                }

                mMap.setOnMarkerClickListener(marker -> {
                    WalkSpot s = (WalkSpot) marker.getTag();
                    if (s != null) {
                        drawRouteFromCurrentLocation(s.lat, s.lng);
                    }
                    return false; // 기본 인포윈도우 표시 유지
                });
            }

            @Override
            public void onFailure(Call<List<WalkSpot>> call, Throwable t) {
                Toast.makeText(MapActivity.this, "통신 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_FAIL", t.getMessage());
            }
        });
    }

    /** ✅ 현재 위치 → 선택 장소까지 길찾기 */
    private void drawRouteFromCurrentLocation(double destLat, double destLng) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                Toast.makeText(MapActivity.this, "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            double startLat = location.getLatitude();
            double startLng = location.getLongitude();

            walkApi.getRoute(startLat, startLng, destLat, destLng, "walking")
                    .enqueue(new Callback<RouteResponse>() {
                        @Override
                        public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                Toast.makeText(MapActivity.this, "경로 요청 실패", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            RouteResponse route = response.body();

                            // ✅ 이전 경로 제거
                            if (currentPolyline != null) currentPolyline.remove();

                            // ✅ 폴리라인 디코딩 및 지도 표시
                            List<LatLng> path = PolyUtil.decode(route.polyline);
                            currentPolyline = mMap.addPolyline(new PolylineOptions()
                                    .addAll(path)
                                    .width(10)
                                    .color(Color.BLUE));

                            // ✅ 카메라를 경로 전체로 이동
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (LatLng p : path) builder.include(p);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 120));

                            // 거리/시간 안내 로그
                            Log.d("ROUTE", "거리: " + route.distance_m + "m, 예상 시간: " + route.duration_s + "초");
                        }

                        @Override
                        public void onFailure(Call<RouteResponse> call, Throwable t) {
                            Toast.makeText(MapActivity.this, "경로 통신 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    /** 위치 권한 요청 */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    /** 권한 요청 결과 처리 */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
