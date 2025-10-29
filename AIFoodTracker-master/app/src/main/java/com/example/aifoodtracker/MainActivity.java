package com.example.aifoodtracker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
// import android.net.Uri; // 사용되지 않는 import 제거
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aifoodtracker.adapter.MealAdapter;
import com.example.aifoodtracker.domain.FoodEntry;
import com.example.aifoodtracker.domain.FoodResponse;
import com.example.aifoodtracker.domain.NutritionInfo;
import com.example.aifoodtracker.domain.User;
import com.example.aifoodtracker.utils.UserPreferenceManager;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

// 🚨🚨🚨 클래스 정의 시작: 모든 코드(변수, 메소드)는 이 중괄호 안에 있어야 합니다! 🚨🚨🚨
public class MainActivity extends AppCompatActivity {

    // --- 멤버 변수 선언 ---
    private TextView tv_user_nickname, tv_total_calories, tv_food_name;
    private ProgressBar pb_total_calories, pb_carbohydrate, pb_protein, pb_fat;
    private TextView tv_gram_of_carbohydrate, tv_gram_of_protein, tv_gram_of_fat;
    private RadarChart radarChart;
    private RecyclerView rv_meal;
    private MealAdapter mealAdapter;
    private ArrayList<FoodEntry> mealList;
    private FloatingActionButton fab_add_meal;
    // private Button btn_find_route; // 지도 버튼 주석 처리
    private Button btn_open_camera;
    private User user;

    // 수정 화면 결과를 받아올 Launcher
    private ActivityResultLauncher<Intent> editFoodLauncher;

    // 누적 변수 (static 유지)
    private static int accumulatedCalories = 0;
    private static int accumulatedCarbGram = 0;
    private static int accumulatedProteinGram = 0;
    private static int accumulatedFatGram = 0;

    // --- onCreate 메소드 (Activity 시작 시 호출) ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize(); // UI 요소 ID 연결
        setupEditFoodLauncher(); // 결과 처리 Launcher 초기화
        loadUserData(getIntent()); // 사용자 정보 로드 (User 객체 설정)
        setupInitialUI(); // UI 초기 설정 (RecyclerView, 그래프 등)
        handleNewFoodEntry(getIntent()); // 새 음식 정보 처리 (있다면)
        addListener(); // 버튼 클릭 리스너 설정
    }

    // --- 초기화 관련 메소드들 ---
    private void initialize() {
        tv_user_nickname = findViewById(R.id.tv_user_nickname);
        tv_total_calories = findViewById(R.id.tv_total_calories);
        tv_food_name = findViewById(R.id.tv_food_name);
        pb_total_calories = findViewById(R.id.pb_total_calories);
        pb_carbohydrate = findViewById(R.id.pb_carbohydrate);
        pb_protein = findViewById(R.id.pb_protein);
        pb_fat = findViewById(R.id.pb_fat);
        tv_gram_of_carbohydrate = findViewById(R.id.tv_gram_of_carbohydrate);
        tv_gram_of_protein = findViewById(R.id.tv_gram_of_protein);
        tv_gram_of_fat = findViewById(R.id.tv_gram_of_fat);
        radarChart = findViewById(R.id.radarchart);
        rv_meal = findViewById(R.id.rv_meal);
        fab_add_meal = findViewById(R.id.fab_add_meal);
        // btn_find_route = findViewById(R.id.btn_find_route); // 주석 처리 유지
        btn_open_camera = findViewById(R.id.btn_open_camera);
    }

    private void setupEditFoodLauncher() {
        editFoodLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        FoodEntry updatedEntry = (FoodEntry) data.getSerializableExtra("updated_food_entry");
                        int position = data.getIntExtra("food_entry_position", -1);

                        if (updatedEntry != null && position != -1 && position < mealList.size()) {
                            mealList.set(position, updatedEntry);
                            mealAdapter.notifyItemChanged(position);
                            recalculateAccumulatedNutrition(); // 누적값 재계산
                            updateNutritionProgressUI(); // UI 갱신
                            Toast.makeText(this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "수정 결과를 처리하지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadUserData(Intent intent) {
        user = intent.getParcelableExtra("user_data");
        if (user == null) {
            user = UserPreferenceManager.getUser(this);
        }
        if (user == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
            finish(); // 사용자 정보 없으면 종료
            return; // 이후 코드 실행 방지
        }
        // UserPreferenceManager에 현재 사용자 정보 저장 (앱 재시작 대비)
        UserPreferenceManager.saveUser(this, user);
    }

    private void setupInitialUI() {
        String welcomeMessage ="AIFoodTracker";
        tv_user_nickname.setText(welcomeMessage);
        settingRecyclerView(); // RecyclerView 설정 호출
        settingBalanceGraph(); // 레이더 차트 초기 설정 호출
    }

    // 🚨🚨🚨 settingRecyclerView 메소드 - 클래스 안에 올바르게 위치해야 함! 🚨🚨🚨
    private void settingRecyclerView() {
        mealList = new ArrayList<>(); // FoodEntry 용 초기화
        // Adapter 생성자에 editFoodLauncher를 세 번째 인자로 전달합니다.
        mealAdapter = new MealAdapter(this, mealList, editFoodLauncher);
        rv_meal.setAdapter(mealAdapter);
        rv_meal.setLayoutManager(new LinearLayoutManager(this));
    }


    // --- 새 음식 처리 및 누적 계산 관련 메소드들 ---
    private void handleNewFoodEntry(Intent intent) {
        FoodResponse foodResponse = (FoodResponse) intent.getSerializableExtra("food_response");
        String imageUriString = intent.getStringExtra("captured_image_uri");

        if (foodResponse != null && foodResponse.getNutritionInfo() != null) {
            String foodName = foodResponse.getFoodName();
            tv_food_name.setText("분석된 음식: " + foodName);

            FoodEntry newEntry = new FoodEntry(foodName, imageUriString, foodResponse.getNutritionInfo());
            mealList.add(0, newEntry);
            mealAdapter.notifyItemInserted(0);
            rv_meal.scrollToPosition(0);

            recalculateAccumulatedNutrition();
            updateNutritionProgressUI();

        } else {
            tv_food_name.setText("분석된 음식: -");
            recalculateAccumulatedNutrition(); // 현재 리스트 기준 누적값 계산 (0일 수 있음)
            updateNutritionProgressUI();
        }
    }

    private void recalculateAccumulatedNutrition() {
        accumulatedCarbGram = 0;
        accumulatedProteinGram = 0;
        accumulatedFatGram = 0;
        accumulatedCalories = 0;

        for (FoodEntry entry : mealList) {
            NutritionInfo nutrition = entry.getNutritionInfo();
            if (nutrition != null) {
                accumulatedCarbGram += (int) nutrition.getCarbohydrate();
                accumulatedProteinGram += (int) nutrition.getProtein();
                accumulatedFatGram += (int) nutrition.getFat();
            }
        }
        accumulatedCalories = (accumulatedCarbGram * 4) + (accumulatedProteinGram * 4) + (accumulatedFatGram * 9);
    }

    private void updateNutritionProgressUI() {
        if (user == null) return;
        updateNutritionProgress(accumulatedCarbGram, accumulatedProteinGram, accumulatedFatGram);
    }

    // --- UI 업데이트 관련 메소드들 ---
    private void addListener() {
        fab_add_meal.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "수동 추가 기능 준비 중", Toast.LENGTH_SHORT).show();
        });

        /* // 지도 버튼 주석 처리 유지
        btn_find_route.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "지도 기능 준비 중입니다.", Toast.LENGTH_SHORT).show();
        });
        */

        btn_open_camera.setOnClickListener(v -> {
            // 사용자 정보가 로드되었는지 확인 후 실행
            if (user != null) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("user_data", user); // 사용자 정보 전달
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "사용자 정보를 로드 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void settingBalanceGraph() {
        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(0f));
        entries.add(new RadarEntry(0f));
        entries.add(new RadarEntry(0f));

        RadarDataSet dataSet = new RadarDataSet(entries, "영양소");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setFillColor(Color.parseColor("#4CAF50"));
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(180);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);

        RadarData data = new RadarData(dataSet);

        XAxis xAxis = radarChart.getXAxis();
        final String[] labels = {"탄수화물", "단백질", "지방"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.DKGRAY);

        YAxis yAxis = radarChart.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setLabelCount(5, true);
        yAxis.setDrawLabels(true);
        yAxis.setTextColor(Color.GRAY);

        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.setTouchEnabled(false);
        radarChart.setData(data);
        radarChart.invalidate();
    }


    private void updateNutritionProgress(int currentTotalCarbGram, int currentTotalProteinGram, int currentTotalFatGram) {
        if (radarChart == null || radarChart.getData() == null || user == null) return;

        int targetCalories = user.getTargetCalories();
        int currentTotalCalories = accumulatedCalories;

        tv_total_calories.setText(currentTotalCalories + " / " + targetCalories + " kcal");
        pb_total_calories.setMax(targetCalories > 0 ? targetCalories : 1);
        pb_total_calories.setProgress(currentTotalCalories);

        int targetCarbGram = targetCalories > 0 ? (int) (targetCalories * 0.5 / 4) : 0;
        int targetProteinGram = targetCalories > 0 ? (int) (targetCalories * 0.3 / 4) : 0;
        int targetFatGram = targetCalories > 0 ? (int) (targetCalories * 0.2 / 9) : 0;

        setProgressBar(pb_carbohydrate, tv_gram_of_carbohydrate, currentTotalCarbGram, targetCarbGram, "g");
        setProgressBar(pb_protein, tv_gram_of_protein, currentTotalProteinGram, targetProteinGram, "g");
        setProgressBar(pb_fat, tv_gram_of_fat, currentTotalFatGram, targetFatGram, "g");

        float carbRatio = targetCarbGram > 0 ? (float) currentTotalCarbGram / targetCarbGram : 0;
        float proteinRatio = targetProteinGram > 0 ? (float) currentTotalProteinGram / targetProteinGram : 0;
        float fatRatio = targetFatGram > 0 ? (float) currentTotalFatGram / targetFatGram : 0;
        updateRadarChart(carbRatio, proteinRatio, fatRatio);
    }

    private void setProgressBar(ProgressBar bar, TextView label, int current, int target, String unit) {
        int maxTarget = Math.max(target, 1);
        bar.setMax(maxTarget);
        bar.setProgress(Math.min(current, maxTarget));
        label.setText(String.format(Locale.KOREA, "%d / %d %s", current, target, unit)); // Locale 적용

        if (target <= 0) {
            bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            return;
        }

        float ratio = (float) current / target;
        if (ratio >= 1.0f) {
            bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
        } else if (ratio >= 0.8f) {
            bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FB8C00")));
        } else {
            bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }
    }

    private void updateRadarChart(float carbRatio, float proteinRatio, float fatRatio) {
        if (radarChart == null || radarChart.getData() == null) return;

        ArrayList<RadarEntry> entries = new ArrayList<>();
        float carbValue = Float.isNaN(carbRatio) || Float.isInfinite(carbRatio) ? 0f : Math.min(carbRatio * 100f, 100f);
        float proteinValue = Float.isNaN(proteinRatio) || Float.isInfinite(proteinRatio) ? 0f : Math.min(proteinRatio * 100f, 100f);
        float fatValue = Float.isNaN(fatRatio) || Float.isInfinite(fatRatio) ? 0f : Math.min(fatRatio * 100f, 100f);

        entries.add(new RadarEntry(carbValue));
        entries.add(new RadarEntry(proteinValue));
        entries.add(new RadarEntry(fatValue));

        RadarDataSet dataSet = (RadarDataSet) radarChart.getData().getDataSetByIndex(0);
        if (dataSet != null) {
            dataSet.setValues(entries);
            radarChart.getData().notifyDataChanged();
            radarChart.notifyDataSetChanged();
        }

        radarChart.invalidate();
    }

// 🚨🚨🚨 클래스 정의 끝: 모든 코드는 이 중괄호 위에 있어야 합니다! 🚨🚨🚨
}

