package com.example.aifoodtracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog; // ⭐️ 경고창(AlertDialog) import
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //  건강 경고 기준값
    /** 사용자의 공복 혈당이 이 값 이상이면 '주의'로 간주 (당뇨 전단계 기준) */
    private static final double DIABETES_SUGAR_THRESHOLD = 100; // mg/dL
    /** 1회 식사의 탄수화물이 이 값을 초과하면 경고 */
    private static final double FOOD_CARB_WARNING_THRESHOLD = 90; // g
    /** 1회 식사의 당류가 이 값을 초과하면 경고 */
    private static final double FOOD_SUGAR_WARNING_THRESHOLD = 25; // g

    // --- UI 변수 ---
    private TextView tv_user_nickname, tv_total_calories, tv_food_name;
    private ProgressBar pb_total_calories, pb_carbohydrate, pb_protein, pb_fat;
    private TextView tv_gram_of_carbohydrate, tv_gram_of_protein, tv_gram_of_fat;
    private RadarChart radarChart;
    private RecyclerView rv_meal;
    private MealAdapter mealAdapter;
    private FloatingActionButton fab_add_meal;
    private Button btn_open_camera;
    private Button btn_reset_data;

    // --- 데이터 변수 ---
    private User user;
    private ArrayList<FoodEntry> mealList;
    private ActivityResultLauncher<Intent> searchFoodLauncher; // '음식 검색'용

    // 누적 변수
    private int accumulatedCalories = 0;
    private int accumulatedCarbGram = 0;
    private int accumulatedProteinGram = 0;
    private int accumulatedFatGram = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate called");

        initialize();
        setupSearchFoodLauncher(); // '음식 검색' Launcher 설정
        loadInitialData(); // User, 누적값, 리스트 로드
        setupInitialUI(); // 로드된 데이터로 UI 설정
        handleNewFoodEntry(getIntent(), "onCreate"); // (중요) 앱이 처음 켜질 때 Intent 처리
        addListener();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent called");
        // 앱이 이미 켜져 있을 때 새 Intent 처리
        handleNewFoodEntry(intent, "onNewIntent");
    }

    // --- 1. 초기화 및 설정 ---

    private void initialize() {
        // (findViewById 코드...)
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
        btn_open_camera = findViewById(R.id.btn_open_camera);
        btn_reset_data = findViewById(R.id.btn_reset_data);
    }

    // '음식 검색' Activity가 종료됐을 때 결과를 처리하는 부분
    private void setupSearchFoodLauncher() {
        searchFoodLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        FoodResponse foodResponse = (FoodResponse) data.getSerializableExtra("searched_food_response");

                        if (foodResponse != null && foodResponse.getNutritionInfo() != null) {
                            Log.e(TAG, "Search result received: " + foodResponse.getFoodName());
                            FoodEntry newEntry = new FoodEntry(foodResponse.getFoodName(), null, foodResponse.getNutritionInfo());

                            mealList.add(0, newEntry);
                            mealAdapter.notifyItemInserted(0);
                            rv_meal.scrollToPosition(0);

                            saveDataAndUpdateUI(); // 누적값 저장 및 UI 갱신

                            checkHealthWarning(newEntry); // (신규) 건강 경고 체크
                        } else {
                            Log.e(TAG, "Failed to get search result from Intent");
                        }
                    } else {
                        Log.e(TAG, "Search was cancelled or failed.");
                    }
                });
    }

    // 앱 시작 시 SharedPreferences에서 모든 데이터 로드
    private void loadInitialData() {
        user = UserPreferenceManager.getUser(this); // 사용자 정보 로드 (혈당 수치 포함)
        if (user == null) {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다. 초기 설정부터 진행해주세요.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        accumulatedCalories = UserPreferenceManager.getAccumulatedCalories(this);
        accumulatedCarbGram = UserPreferenceManager.getAccumulatedCarb(this);
        accumulatedProteinGram = UserPreferenceManager.getAccumulatedProtein(this);
        accumulatedFatGram = UserPreferenceManager.getAccumulatedFat(this);
        Log.e(TAG, "Loaded Accumulated - Cal: " + accumulatedCalories + ", Carb: " + accumulatedCarbGram);
        mealList = UserPreferenceManager.getMealList(this);
        Log.e(TAG, "Loaded mealList size: " + (mealList != null ? mealList.size() : 0));
    }

    // 로드된 데이터로 UI 초기 설정
    private void setupInitialUI() {
        if (user == null) return;
        String welcomeMessage ="AIFoodTracker";
        tv_user_nickname.setText(welcomeMessage);
        settingRecyclerView();
        settingBalanceGraph();
        updateNutritionProgressUI();
    }

    // RecyclerView 설정
    private void settingRecyclerView() {
        if (mealList == null) {
            mealList = new ArrayList<>();
        }
        mealAdapter = new MealAdapter(this, mealList);
        rv_meal.setAdapter(mealAdapter);
        rv_meal.setLayoutManager(new LinearLayoutManager(this));
    }


    // 새 음식 처리 (카메라/갤러리로부터) ---
    private void handleNewFoodEntry(Intent intent, String calledFrom) {
        if (intent == null) return;

        // Intent에서 새 음식 정보 가져오기
        FoodResponse foodResponse = (FoodResponse) intent.getSerializableExtra("food_response");
        String imageUriString = intent.getStringExtra("captured_image_uri");

        if (foodResponse != null && foodResponse.getNutritionInfo() != null) {
            Log.e(TAG, "New food entry (from Camera) found in Intent (from " + calledFrom + ")");
            String foodName = foodResponse.getFoodName();
            tv_food_name.setText("분석된 음식: " + foodName); // 마지막 음식 이름 표시

            FoodEntry newEntry = new FoodEntry(foodName, imageUriString, foodResponse.getNutritionInfo());

            mealList.add(0, newEntry); // 리스트에 추가
            mealAdapter.notifyItemInserted(0);
            rv_meal.scrollToPosition(0);

            saveDataAndUpdateUI(); // 누적값 저장 및 UI 갱신

            checkHealthWarning(newEntry); // (신규) 건강 경고 체크

            // (중요) 처리된 Intent 데이터를 지워서 중복 추가 방지
            intent.removeExtra("food_response");
            intent.removeExtra("captured_image_uri");
        } else {
            Log.e(TAG, "No new food entry (from Camera) found in Intent (from " + calledFrom + ")");
        }
    }

    //  건강 경고 체크 함수 ---
    private void checkHealthWarning(FoodEntry newEntry) {
        if (user == null || newEntry == null || newEntry.getNutritionInfo() == null) {
            Log.e(TAG, "checkHealthWarning: User or NutritionInfo is null, skipping check.");
            return;
        }

        // 1. 사용자 상태 확인 (공복 혈당 100 이상 시 '주의'로 간주)
        double userBloodSugar = user.getBloodSugar();
        boolean userIsAtRisk = (userBloodSugar >= DIABETES_SUGAR_THRESHOLD);

        if (!userIsAtRisk) {
            Log.e(TAG, "checkHealthWarning: User is not in at-risk group (BloodSugar: " + userBloodSugar + ")");
            return; // 사용자가 '주의' 대상이 아니면 경고 안 함
        }

        Log.e(TAG, "checkHealthWarning: User is in at-risk group (BloodSugar: " + userBloodSugar + ")");

        // 2. 새로 추가된 음식의 영양 정보 확인
        NutritionInfo nutrition = newEntry.getNutritionInfo();
        double foodCarbs = nutrition.getCarbohydrate();
        double foodSugar = nutrition.getSugar(); // ⭐️ getSugar()가 NutritionInfo.java에 있어야 함

        String warningMessage = "";
        if (foodCarbs > FOOD_CARB_WARNING_THRESHOLD) {
            warningMessage += "• 이 음식은 탄수화물이 1회 섭취량(" + (int)FOOD_CARB_WARNING_THRESHOLD + "g)을 초과합니다. (현재: " + (int)foodCarbs + "g)\n";
        }
        if (foodSugar > FOOD_SUGAR_WARNING_THRESHOLD) {
            warningMessage += "• 이 음식은 당류가 1회 섭취량(" + (int)FOOD_SUGAR_WARNING_THRESHOLD + "g)을 초과합니다. (현재: " + (int)foodSugar + "g)\n";
        }

        // 3. 경고 메시지가 있으면 AlertDialog 띄우기
        if (!warningMessage.isEmpty()) {
            Log.e(TAG, "checkHealthWarning: Showing warning dialog for " + newEntry.getFoodName());
            new AlertDialog.Builder(this)
                    .setTitle("탄수화물 경고!")
                    .setMessage("회원님은 혈당 관리가 필요한 상태입니다.\n\n" +
                            "[" + newEntry.getFoodName() + "] 의 영양 정보:\n" +
                            warningMessage + "\n섭취에 유의하세요.")
                    .setPositiveButton("확인", null) // null = 그냥 팝업 닫기
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            Log.e(TAG, "checkHealthWarning: Food " + newEntry.getFoodName() + " is within thresholds.");
        }
    }


    // 4. 데이터 저장 및 UI 갱신 (변경 없음)
    private void saveDataAndUpdateUI() {
        Log.e(TAG, "saveDataAndUpdateUI called. Current mealList size: " + mealList.size());
        recalculateAccumulatedNutrition(); // 리스트 기반으로 누적값 재계산

        Log.e(TAG, "Saving Accumulated - Cal: " + accumulatedCalories + ", Carb: " + accumulatedCarbGram);
        UserPreferenceManager.saveAccumulatedValues(this, accumulatedCalories, accumulatedCarbGram, accumulatedProteinGram, accumulatedFatGram);
        UserPreferenceManager.saveMealList(this, mealList);

        updateNutritionProgressUI(); // UI 갱신
    }

    private void recalculateAccumulatedNutrition() {
        if (mealList == null) {
            mealList = new ArrayList<>();
        }
        Log.e(TAG, "Recalculating nutrition. List size: " + mealList.size());

        accumulatedCalories = 0;
        accumulatedCarbGram = 0;
        accumulatedProteinGram = 0;
        accumulatedFatGram = 0;

        for (FoodEntry entry : mealList) {
            NutritionInfo nutrition = entry.getNutritionInfo();
            if (nutrition != null) {
                accumulatedCarbGram += (int) nutrition.getCarbohydrate();
                accumulatedProteinGram += (int) nutrition.getProtein();
                accumulatedFatGram += (int) nutrition.getFat();
            }
        }
        accumulatedCalories = (accumulatedCarbGram * 4) + (accumulatedProteinGram * 4) + (accumulatedFatGram * 9);
        Log.e(TAG, "Recalculated Accumulated - Cal: " + accumulatedCalories + ", Carb: " + accumulatedCarbGram);
    }

    private void updateNutritionProgressUI() {
        if (user == null) return;
        updateNutritionProgress(accumulatedCarbGram, accumulatedProteinGram, accumulatedFatGram);
    }

    // --- 5. 나머지 메소드들 (변경 없음) ---
    private void addListener() {
        // '+' 버튼 (음식 검색)
        fab_add_meal.setOnClickListener(v -> {
            Log.e(TAG, "FAB clicked, launching SearchFoodActivity...");
            Intent intent = new Intent(MainActivity.this, SearchFoodActivity.class);
            searchFoodLauncher.launch(intent); // Launcher로 실행
        });

        // (지도 버튼은 주석 처리 유지)

        // 카메라 버튼
        btn_open_camera.setOnClickListener(v -> {
            if (user != null) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "사용자 정보를 로드 중입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 초기화 버튼
        btn_reset_data.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("기록 초기화")
                    .setMessage("오늘의 식단 기록과 누적 칼로리를 모두 삭제하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> resetDailyData())
                    .setNegativeButton("아니요", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });
    }

    private void resetDailyData() {
        accumulatedCalories = 0;
        accumulatedCarbGram = 0;
        accumulatedProteinGram = 0;
        accumulatedFatGram = 0;

        if (mealList != null) {
            mealList.clear();
        } else {
            mealList = new ArrayList<>();
        }

        if (mealAdapter != null) {
            mealAdapter.notifyDataSetChanged(); // 어댑터 갱신 (리스트가 비었음을 알림)
        }

        // SharedPreferences에도 초기화된 값 저장!
        UserPreferenceManager.saveAccumulatedValues(this, 0, 0, 0, 0);
        UserPreferenceManager.saveMealList(this, mealList);

        updateNutritionProgressUI(); // UI 갱신 (0으로)
        tv_food_name.setText("분석된 음식: -");

        Log.e(TAG, "Daily data has been reset.");
        Toast.makeText(this, "기록이 초기화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void settingBalanceGraph() {
        // (이전과 동일)
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
        // (이전과 동일)
        if (radarChart == null || radarChart.getData() == null || user == null) return;
        int targetCalories = user.getTargetCalories();
        int currentTotalCalories = accumulatedCalories;
        tv_total_calories.setText(String.format(Locale.KOREA, "%d / %d kcal", currentTotalCalories, targetCalories));
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
        // (이전과 동일)
        int maxTarget = Math.max(target, 1);
        bar.setMax(maxTarget);
        bar.setProgress(current);
        label.setText(String.format(Locale.KOREA, "%d / %d %s", current, target, unit));
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
        // (이전과 동일)
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
}

