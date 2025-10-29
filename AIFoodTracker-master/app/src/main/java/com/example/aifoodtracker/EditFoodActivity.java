package com.example.aifoodtracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // TextUtils import 추가
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aifoodtracker.domain.FoodEntry;
import com.example.aifoodtracker.domain.NutritionInfo; // NutritionInfo import 추가

import java.util.Locale; // Locale import 추가

public class EditFoodActivity extends AppCompatActivity {

    private EditText etEditFoodName, etEditCalories, etEditCarbs, etEditProtein, etEditFat;
    private Button btnCancelEdit, btnSaveEdit;

    private FoodEntry originalFoodEntry; // 수정 전 원본 데이터
    private int entryPosition = -1; // 리스트에서의 위치

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_food);

        // UI 요소 연결
        etEditFoodName = findViewById(R.id.et_edit_food_name);
        etEditCalories = findViewById(R.id.et_edit_calories);
        etEditCarbs = findViewById(R.id.et_edit_carbs);
        etEditProtein = findViewById(R.id.et_edit_protein);
        etEditFat = findViewById(R.id.et_edit_fat);
        btnCancelEdit = findViewById(R.id.btn_cancel_edit);
        btnSaveEdit = findViewById(R.id.btn_save_edit);

        // Intent로부터 데이터 받기
        Intent intent = getIntent();
        originalFoodEntry = (FoodEntry) intent.getSerializableExtra("food_entry_to_edit");
        entryPosition = intent.getIntExtra("food_entry_position", -1);

        // 받은 데이터가 유효하면 화면에 표시
        if (originalFoodEntry != null && originalFoodEntry.getNutritionInfo() != null) {
            populateFields(originalFoodEntry);
        } else {
            // 데이터가 없으면 오류 메시지 표시 후 종료 (예외 처리)
            Toast.makeText(this, "수정할 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 액티비티 종료
            return; // 아래 코드 실행 방지
        }

        // 버튼 리스너 설정
        setupButtonClickListeners();
    }

    // 입력 필드에 기존 데이터 채우기
    private void populateFields(FoodEntry entry) {
        etEditFoodName.setText(entry.getFoodName());
        NutritionInfo nutrition = entry.getNutritionInfo();
        // 소수점 첫째 자리까지 표시 (한국 로케일 기준)
        etEditCalories.setText(String.format(Locale.KOREA, "%.1f", nutrition.getCalories()));
        etEditCarbs.setText(String.format(Locale.KOREA, "%.1f", nutrition.getCarbohydrate()));
        etEditProtein.setText(String.format(Locale.KOREA, "%.1f", nutrition.getProtein()));
        etEditFat.setText(String.format(Locale.KOREA, "%.1f", nutrition.getFat()));
    }

    // 버튼 클릭 리스너 설정
    private void setupButtonClickListeners() {
        // 취소 버튼: 아무 작업 없이 현재 액티비티 종료
        btnCancelEdit.setOnClickListener(v -> {
            setResult(RESULT_CANCELED); // 취소되었음을 알림 (선택 사항)
            finish();
        });

        // 저장 버튼: 입력 값 검증 후 결과 돌려주기
        btnSaveEdit.setOnClickListener(v -> {
            saveChanges();
        });
    }

    // 변경 사항 저장 로직
    private void saveChanges() {
        // 입력 값 가져오기
        String foodName = etEditFoodName.getText().toString().trim();
        String caloriesStr = etEditCalories.getText().toString().trim();
        String carbsStr = etEditCarbs.getText().toString().trim();
        String proteinStr = etEditProtein.getText().toString().trim();
        String fatStr = etEditFat.getText().toString().trim();

        // 필수 입력 값 검증
        if (TextUtils.isEmpty(foodName) || TextUtils.isEmpty(caloriesStr) || TextUtils.isEmpty(carbsStr) ||
                TextUtils.isEmpty(proteinStr) || TextUtils.isEmpty(fatStr)) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 문자열을 숫자로 변환
            double calories = Double.parseDouble(caloriesStr);
            double carbs = Double.parseDouble(carbsStr);
            double protein = Double.parseDouble(proteinStr);
            double fat = Double.parseDouble(fatStr);

            // NutritionInfo 객체 새로 생성 (또는 기존 객체 수정도 가능)
            NutritionInfo updatedNutrition = new NutritionInfo();
            updatedNutrition.setCalories(calories);
            updatedNutrition.setCarbohydrate(carbs);
            updatedNutrition.setProtein(protein);
            updatedNutrition.setFat(fat);
            // sugar 등 다른 필드는 없으므로 기본값(0 또는 null) 유지

            // FoodEntry 객체 새로 생성 (새로운 timestamp 적용됨)
            // 이미지 URI는 원본 유지
            FoodEntry updatedFoodEntry = new FoodEntry(foodName, originalFoodEntry.getImageUri(), updatedNutrition);

            // 결과를 담을 Intent 생성
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_food_entry", updatedFoodEntry);
            resultIntent.putExtra("food_entry_position", entryPosition); // 원래 위치 정보 포함

            // 결과 설정 (RESULT_OK: 성공적으로 완료됨) 및 액티비티 종료
            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (NumberFormatException e) {
            // 숫자 변환 실패 시 오류 메시지
            Toast.makeText(this, "숫자 입력 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
    
