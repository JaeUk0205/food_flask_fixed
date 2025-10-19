package com.example.aifoodtracker;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

    private TextView tv_user_nickname, tv_total_calories;
    private ProgressBar pb_total_calories;
    private RadarChart radarChart;
    private RecyclerView rv_meal;
    private MealAdapter mealAdapter;
    private ArrayList<String> mealList;
    private FloatingActionButton fab_add_meal;
    private ProgressBar pb_carbohydrate, pb_protein, pb_fat;
    private TextView tv_gram_of_carbohydrate, tv_gram_of_protein, tv_gram_of_fat;
    private Button btn_find_route;
    private User user;
    private Uri capturedImageUri;

    private Handler handler = new Handler();
    private Runnable blinkRunnable;
    private boolean isBlinking = false;
    private int blinkColor = Color.RED;
    private int normalColor = Color.parseColor("#4CAF50");
    private float WARNING_THRESHOLD_PERCENT = 80f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user_data");

        String uriString = intent.getStringExtra("captured_image_uri");
        if (uriString != null) {
            capturedImageUri = Uri.parse(uriString);
            Toast.makeText(this, "촬영된 이미지: " + capturedImageUri.toString(), Toast.LENGTH_LONG).show();
        }

        if (user != null) {
            setting();
            addListener();

            FoodResponse foodResponse = (FoodResponse) intent.getSerializableExtra("food_response");

            if (foodResponse != null) {
                int carbGram = (int) foodResponse.getCarbohydrate();
                int proteinGram = (int) foodResponse.getProtein();
                int fatGram = (int) foodResponse.getFat();
                updateNutritionProgress(carbGram, proteinGram, fatGram);
            } else {
                updateNutritionProgress(150, 70, 45);
            }
        } else {
            Toast.makeText(this, "사용자 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initialize() {
        tv_user_nickname = findViewById(R.id.tv_user_nickname);
        tv_total_calories = findViewById(R.id.tv_total_calories);
        pb_total_calories = findViewById(R.id.pb_total_calories);
        radarChart = findViewById(R.id.radarchart);
        rv_meal = findViewById(R.id.rv_meal);
        fab_add_meal = findViewById(R.id.fab_add_meal);
        pb_carbohydrate = findViewById(R.id.pb_carbohydrate);
        tv_gram_of_carbohydrate = findViewById(R.id.tv_gram_of_carbohydrate);
        pb_protein = findViewById(R.id.pb_protein);
        tv_gram_of_protein = findViewById(R.id.tv_gram_of_protein);
        pb_fat = findViewById(R.id.pb_fat);
        tv_gram_of_fat = findViewById(R.id.tv_gram_of_fat);
        btn_find_route = findViewById(R.id.btn_find_route);
    }

    private void setting() {
        String welcomeMessage = user.getGender() + " 지옥의 다이어트 시작하셔야 합니다!!";
        tv_user_nickname.setText(welcomeMessage);
        settingBalanceGraph();
        settingRecyclerView();
    }

    private void addListener() {
        fab_add_meal.setOnClickListener(listener_add_meal);

        btn_find_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }

    private final View.OnClickListener listener_add_meal = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int mealNumber = mealList.size() + 1;
            mealList.add("식사 " + mealNumber);
            mealAdapter.notifyItemInserted(mealList.size() - 1);
        }
    };

    private void settingBalanceGraph() {
        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(10f));
        entries.add(new RadarEntry(10f));
        entries.add(new RadarEntry(10f));

        RadarDataSet dataSet = new RadarDataSet(entries, "영양소");
        dataSet.setColor(normalColor);
        dataSet.setFillColor(normalColor);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(180);
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setDrawValues(true);

        RadarData data = new RadarData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.DKGRAY);

        XAxis xAxis = radarChart.getXAxis();
        final String[] labels = {"탄수화물", "단백질", "지방"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.BLACK);

        YAxis yAxis = radarChart.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setDrawLabels(false);

        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.setTouchEnabled(false);
        radarChart.animateXY(1400, 1400);

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
        int targetCalories = user.getTargetCalories();
        int currentCalories = (currentCarbGram * 4) + (currentProteinGram * 4) + (currentFatGram * 9);
        tv_total_calories.setText(currentCalories + " / " + targetCalories + " kcal");
        pb_total_calories.setMax(targetCalories);
        pb_total_calories.setProgress(currentCalories);

        int targetCarbGram = (int) (targetCalories * 0.5 / 4);
        int targetProteinGram = (int) (targetCalories * 0.3 / 4);
        int targetFatGram = (int) (targetCalories * 0.2 / 9);

        pb_carbohydrate.setMax(targetCarbGram);
        pb_carbohydrate.setProgress(currentCarbGram);
        tv_gram_of_carbohydrate.setText(currentCarbGram + " / " + targetCarbGram + " g");

        pb_protein.setMax(targetProteinGram);
        pb_protein.setProgress(currentProteinGram);
        tv_gram_of_protein.setText(currentProteinGram + " / " + targetProteinGram + " g");

        pb_fat.setMax(targetFatGram);
        pb_fat.setProgress(currentFatGram);
        tv_gram_of_fat.setText(currentFatGram + " / " + targetFatGram + " g");

        float carbPercent = (targetCarbGram > 0) ? (float) currentCarbGram / targetCarbGram * 100 : 0;
        float proteinPercent = (targetProteinGram > 0) ? (float) currentProteinGram / targetProteinGram * 100 : 0;
        float fatPercent = (targetFatGram > 0) ? (float) currentFatGram / targetFatGram * 100 : 0;

        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(carbPercent > 0 ? carbPercent : 10f));
        entries.add(new RadarEntry(proteinPercent > 0 ? proteinPercent : 10f));
        entries.add(new RadarEntry(fatPercent > 0 ? fatPercent : 10f));

        RadarDataSet dataSet = (RadarDataSet) radarChart.getData().getDataSetByIndex(0);
        dataSet.setValues(entries);

        boolean hasWarning = carbPercent > WARNING_THRESHOLD_PERCENT || proteinPercent > WARNING_THRESHOLD_PERCENT || fatPercent > WARNING_THRESHOLD_PERCENT;

        if (hasWarning && !isBlinking) {
            startBlinking(dataSet);
        } else if (!hasWarning && isBlinking) {
            stopBlinking(dataSet);
        } else if (!hasWarning) {
            dataSet.setColor(normalColor);
            dataSet.setFillColor(normalColor);
        }

        radarChart.getData().notifyDataChanged();
        radarChart.notifyDataSetChanged();
        radarChart.invalidate();
    }

    private void startBlinking(RadarDataSet dataSet) {
        isBlinking = true;
        blinkRunnable = new Runnable() {
            boolean alternate = false;
            @Override
            public void run() {
                if(dataSet == null) return;
                if (alternate) {
                    dataSet.setColor(blinkColor);
                    dataSet.setFillColor(blinkColor);
                } else {
                    dataSet.setColor(normalColor);
                    dataSet.setFillColor(normalColor);
                }
                alternate = !alternate;
                radarChart.getData().notifyDataChanged();
                radarChart.notifyDataSetChanged();
                radarChart.invalidate();
                handler.postDelayed(this, 500);
            }
        };
        handler.post(blinkRunnable);
    }

    private void stopBlinking(RadarDataSet dataSet) {
        isBlinking = false;
        if (blinkRunnable != null) {
            handler.removeCallbacks(blinkRunnable);
        }
        if(dataSet != null) {
            dataSet.setColor(normalColor);
            dataSet.setFillColor(normalColor);
            radarChart.getData().notifyDataChanged();
            radarChart.notifyDataSetChanged();
            radarChart.invalidate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && blinkRunnable != null) {
            handler.removeCallbacks(blinkRunnable);
        }
    }
}