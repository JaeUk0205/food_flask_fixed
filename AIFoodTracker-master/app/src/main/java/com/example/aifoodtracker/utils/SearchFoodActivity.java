package com.example.aifoodtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aifoodtracker.adapter.SearchAdapter;
import com.example.aifoodtracker.domain.FoodResponse;
import com.example.aifoodtracker.network.RetrofitAPI;
import com.example.aifoodtracker.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFoodActivity extends AppCompatActivity {

    private static final String TAG = "SearchFoodActivity";
    private EditText etSearchQuery;
    private RecyclerView rvSearchResults;
    private SearchAdapter searchAdapter;
    private ProgressBar pbSearchLoading;
    private TextView tvNoResults;
    private RetrofitAPI apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_food);

        // UI 요소 초기화
        etSearchQuery = findViewById(R.id.et_search_query);
        rvSearchResults = findViewById(R.id.rv_search_results);
        pbSearchLoading = findViewById(R.id.pb_search_loading);
        tvNoResults = findViewById(R.id.tv_no_results);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_search);

        // 툴바 뒤로가기 버튼
        toolbar.setNavigationOnClickListener(v -> finish());

        // Retrofit 서비스 초기화
        apiService = RetrofitClient.getApiService();

        // RecyclerView 설정
        setupRecyclerView();

        // 검색창 리스너 설정
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchFood(query); // 2글자 이상일 때 검색 시작
                } else {
                    pbSearchLoading.setVisibility(View.GONE);
                    tvNoResults.setVisibility(View.GONE);
                    rvSearchResults.setVisibility(View.VISIBLE);
                    searchAdapter.updateResults(null); // 2글자 미만이면 목록 비우기
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        // ⭐️ 어댑터에 '아이템 클릭' 리스너 전달
        searchAdapter = new SearchAdapter(this, foodResponse -> {
            // ⭐️ 사용자가 항목을 클릭했을 때 실행되는 부분
            Log.d(TAG, "Selected food: " + foodResponse.getFoodName());

            // 1. 결과 Intent 생성
            Intent resultIntent = new Intent();
            // 2. 선택된 음식 정보(FoodResponse)를 Intent에 담기
            resultIntent.putExtra("searched_food_response", foodResponse);
            // 3. MainActivity에 '성공' 결과와 데이터 돌려주기
            setResult(Activity.RESULT_OK, resultIntent);
            // 4. 현재 화면 종료
            finish();
        });
        rvSearchResults.setAdapter(searchAdapter);
    }

    // ⭐️ 서버에 음식 검색 요청
    private void searchFood(String query) {
        Log.d(TAG, "Searching for: " + query);
        pbSearchLoading.setVisibility(View.VISIBLE); // 로딩 시작
        tvNoResults.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);

        apiService.searchFoodByName(query).enqueue(new Callback<List<FoodResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<FoodResponse>> call, @NonNull Response<List<FoodResponse>> response) {
                pbSearchLoading.setVisibility(View.GONE); // 로딩 종료
                rvSearchResults.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<FoodResponse> results = response.body();
                    Log.d(TAG, "Search results: " + results.size() + " items");
                    if (results.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE); // "결과 없음" 표시
                    }
                    searchAdapter.updateResults(results); // 어댑터에 결과 업데이트
                } else {
                    Log.e(TAG, "Server response error: " + response.code());
                    Toast.makeText(SearchFoodActivity.this, "서버 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    tvNoResults.setVisibility(View.VISIBLE);
                    searchAdapter.updateResults(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FoodResponse>> call, @NonNull Throwable t) {
                pbSearchLoading.setVisibility(View.GONE); // 로딩 종료
                tvNoResults.setVisibility(View.VISIBLE);
                Log.e(TAG, "Network failure: " + t.getMessage());
                Toast.makeText(SearchFoodActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                searchAdapter.updateResults(null);
            }
        });
    }
}
