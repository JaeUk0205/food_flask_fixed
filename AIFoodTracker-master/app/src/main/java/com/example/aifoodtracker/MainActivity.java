package com.example.aifoodtracker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aifoodtracker.adapter.MealAdapter;
import com.example.aifoodtracker.domain.FoodResponse;
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

public class MainActivity extends AppCompatActivity {

    private TextView tv_user_nickname, tv_total_calories, tv_food_name;
    private ProgressBar pb_total_calories, pb_carbohydrate, pb_protein, pb_fat;
    private TextView tv_gram_of_carbohydrate, tv_gram_of_protein, tv_gram_of_fat;
    private RadarChart radarChart;
    private RecyclerView rv_meal;
    private MealAdapter mealAdapter;
    private ArrayList<String> mealList;
    private FloatingActionButton fab_add_meal;
    private Button btn_find_route, btn_open_camera;

    private User user;
    private Uri capturedImageUri;
    private Handler handler = new Handler();
    private Runnable blinkRunnable;
    private boolean isBlinking = false;
    private int blinkColor = Color.RED;
    private int normalColor = Color.parseColor("#4CAF50");
    private float WARNING_THRESHOLD_PERCENT = 85f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user_data");

        // SharedPreferences에서 복원 (user가 null일 때 대비)
        if (user == null) {
            user = UserPreferenceManager.getUser(this);
        }

        // 그래도 없으면 앱 종료
        if (user == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 그래프 세팅 먼저 (여기 순서가 중요!)
        setting();

        // 음식 분석 결과 수신
        FoodResponse foodResponse = (FoodResponse) intent.getSerializableExtra("food_response");
        if (foodResponse != null) {
            String foodName = foodResponse.getFoodName();
            tv_food_name.setText("분석된 음식: " + foodName);

            int carbGram = (int) foodResponse.getNutritionInfo().getCarbohydrate();
            int proteinGram = (int) foodResponse.getNutritionInfo().getProtein();
            int fatGram = (int) foodResponse.getNutritionInfo().getFat();

            updateNutritionProgress(carbGram, proteinGram, fatGram);
        } else {
            tv_food_name.setText("분석된 음식: -");
            // ✅ 수정된 부분: 초기 섭취량을 모두 0으로 설정
            updateNutritionProgress(0, 0, 0);
        }

        addListener();
    }

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
        btn_find_route = findViewById(R.id.btn_find_route);
        btn_open_camera = findViewById(R.id.btn_open_camera);
    }

    private void setting() {
        String welcomeMessage ="AIFoodTracker";
        tv_user_nickname.setText(welcomeMessage);
        settingBalanceGraph();
        settingRecyclerView();
    }

    private void addListener() {
        fab_add_meal.setOnClickListener(v -> {
            int mealNumber = mealList.size() + 1;
            mealList.add("식사 " + mealNumber);
            mealAdapter.notifyItemInserted(mealList.size() - 1);
        });

        btn_find_route.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // 음식 분석 버튼 → 카메라 실행
        btn_open_camera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra("user_data", user);
            startActivity(intent);
        });
    }

    private void settingBalanceGraph() {
        // 초기에는 0f로 설정하여 비어있는 그래프로 시작합니다.
        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(0f));
        entries.add(new RadarEntry(0f));
        entries.add(new RadarEntry(0f));

        RadarDataSet dataSet = new RadarDataSet(entries, "영양소");
        dataSet.setColor(normalColor);
        dataSet.setFillColor(normalColor);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(180);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(true);

        RadarData data = new RadarData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.DKGRAY);

        XAxis xAxis = radarChart.getXAxis();
        final String[] labels = {"탄수화물", "단백질", "지방"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextSize(13f);
        xAxis.setTextColor(Color.BLACK);

        YAxis yAxis = radarChart.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setDrawLabels(false);

        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.setTouchEnabled(false);
        radarChart.setData(data);
        radarChart.invalidate();
    }

    private void settingRecyclerView() {
        mealList = new ArrayList<>();
        mealAdapter = new MealAdapter(this, mealList);
        rv_meal.setAdapter(mealAdapter);
        rv_meal.setLayoutManager(new LinearLayoutManager(this));
        mealList.add("식사 1");
        mealAdapter.notifyItemInserted(0);
    }

    private void updateNutritionProgress(int currentCarbGram, int currentProteinGram, int currentFatGram) {
        // 혹시라도 radarChart 초기화가 안 됐을 경우 대비
        if (radarChart.getData() == null) return;

        // 1. 목표 칼로리 및 섭취 칼로리 계산
        int targetCalories = user.getTargetCalories();
        int currentCalories = (currentCarbGram * 4) + (currentProteinGram * 4) + (currentFatGram * 9);

        tv_total_calories.setText(currentCalories + " / " + targetCalories + " kcal");
        pb_total_calories.setMax(targetCalories);
        pb_total_calories.setProgress(currentCalories);

        // 2. 목표 영양소 그램 계산 (총 목표 칼로리에 의해 결정됨)
        int targetCarbGram = (int) (targetCalories * 0.5 / 4);
        int targetProteinGram = (int) (targetCalories * 0.3 / 4);
        int targetFatGram = (int) (targetCalories * 0.2 / 9);

        // 3. 프로그레스 바 및 텍스트 업데이트
        setProgressBar(pb_carbohydrate, tv_gram_of_carbohydrate, currentCarbGram, targetCarbGram, "g");
        setProgressBar(pb_protein, tv_gram_of_protein, currentProteinGram, targetProteinGram, "g");
        setProgressBar(pb_fat, tv_gram_of_fat, currentFatGram, targetFatGram, "g");

        // 4. 레이더 차트 업데이트
        updateRadarChart((float) currentCarbGram / targetCarbGram, (float) currentProteinGram / targetProteinGram, (float) currentFatGram / targetFatGram);
    }

    private void setProgressBar(ProgressBar bar, TextView label, int current, int target, String unit) {
        // 목표량이 0일 경우 (예외 처리)
        if (target <= 0) {
            bar.setMax(1); // 0으로 나누는 것을 방지
            bar.setProgress(current > 0 ? 1 : 0);
            label.setText(current + " / " + target + " " + unit);
            return;
        }

        bar.setMax(target);
        bar.setProgress(current);
        label.setText(current + " / " + target + " " + unit);

        float ratio = (float) current / target;
        if (ratio >= 1.0f) {
            bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#E53935"))); // 초과 (빨강)
        } else if (ratio >= 0.8f) {
            bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FB8C00"))); // 주의 (주황)
        } else {
            bar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // 정상 (초록)
        }
    }

    private void updateRadarChart(float carbRatio, float proteinRatio, float fatRatio) {
        ArrayList<RadarEntry> entries = new ArrayList<>();

        // 목표 그램이 0일 경우 (0으로 나누는 것 방지)
        float carbValue = Float.isInfinite(carbRatio) || Float.isNaN(carbRatio) ? 0 : carbRatio * 100;
        float proteinValue = Float.isInfinite(proteinRatio) || Float.isNaN(proteinRatio) ? 0 : proteinRatio * 100;
        float fatValue = Float.isInfinite(fatRatio) || Float.isNaN(fatRatio) ? 0 : fatRatio * 100;

        entries.add(new RadarEntry(carbValue));
        entries.add(new RadarEntry(proteinValue));
        entries.add(new RadarEntry(fatValue));

        RadarDataSet dataSet = (RadarDataSet) radarChart.getData().getDataSetByIndex(0);
        dataSet.setValues(entries);

        radarChart.getData().notifyDataChanged();
        radarChart.notifyDataSetChanged();
        radarChart.invalidate();
    }
}