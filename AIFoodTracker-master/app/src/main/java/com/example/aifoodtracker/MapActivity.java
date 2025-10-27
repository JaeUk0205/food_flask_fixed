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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private double foodCalories = 580.0; // AI 분석 결과에서 전달받을 값

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void startLocationUpdates() {
        try {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    LocationRequest.PRIORITY_HIGH_ACCURACY, 4000
            ).build();

            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        LatLng myPos = new LatLng(lat, lng);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 14));

                        loadWalkRoutes(lat, lng, foodCalories);
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            }, getMainLooper());

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void loadWalkRoutes(double lat, double lng, double calories) {
        RetrofitAPI api = RetrofitClient.getApiService();
        Call<WalkResponse> call = api.getWalks(lat, lng, calories);

        call.enqueue(new Callback<WalkResponse>() {
            @Override
            public void onResponse(Call<WalkResponse> call, Response<WalkResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WalkResponse data = response.body();
                    List<WalkRoute> nearby = data.getNearbyRoutes();
                    List<WalkRoute> famous = data.getFamousRoutes();

                    runOnUiThread(() -> {
                        if (nearby != null) {
                            for (WalkRoute route : nearby) {
                                LatLng point = new LatLng(route.getLat(), route.getLng());
                                mMap.addMarker(new MarkerOptions()
                                        .position(point)
                                        .title(route.getName())
                                        .snippet("거리: " + route.getDistance() + " km (근처 코스)"));
                            }
                        }

                        if (famous != null) {
                            for (WalkRoute route : famous) {
                                LatLng point = new LatLng(route.getLat(), route.getLng());
                                mMap.addMarker(new MarkerOptions()
                                        .position(point)
                                        .title("⭐ " + route.getName())
                                        .snippet("도시: " + route.getCity() + " / 거리: " + route.getDistance() + " km"));
                            }
                        }

                        mMap.setOnMarkerClickListener(marker -> {
                            marker.showInfoWindow(); // 클릭 시 정보창 표시
                            return true;
                        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
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