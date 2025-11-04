package com.example.aifoodtracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils; // TextUtils import 추가
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aifoodtracker.domain.User;
import com.example.aifoodtracker.utils.UserPreferenceManager; // UserPreferenceManager import 추가

import java.util.UUID;

public class InitialSurveyActivity extends AppCompatActivity {

    private RadioGroup rg_gender;
    private EditText et_height, et_weight;
    private EditText et_blood_pressure_sys, et_blood_pressure_dia, et_blood_sugar; // 혈압/혈당 EditText
    private Button btn_start;
    private TextView tv_bmi_result;

    private boolean isResultShown = false;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_survey);

        rg_gender = findViewById(R.id.rg_gender);
        et_height = findViewById(R.id.et_height);
        et_weight = findViewById(R.id.et_weight);
        // 혈압/혈당 ID 연결
        et_blood_pressure_sys = findViewById(R.id.et_blood_pressure_sys);
        et_blood_pressure_dia = findViewById(R.id.et_blood_pressure_dia);
        et_blood_sugar = findViewById(R.id.et_blood_sugar);
        btn_start = findViewById(R.id.btn_start);
        tv_bmi_result = findViewById(R.id.tv_bmi_result);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isResultShown) {
                    // --- 1. 필수 값 (키, 몸무게, 성별) 검사 ---
                    String heightStr = et_height.getText().toString().trim();
                    String weightStr = et_weight.getText().toString().trim();

                    if (heightStr.isEmpty() || weightStr.isEmpty()) {
                        Toast.makeText(InitialSurveyActivity.this, "키와 몸무게를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double height = Double.parseDouble(heightStr);
                    double weight = Double.parseDouble(weightStr);

                    int selectedGenderId = rg_gender.getCheckedRadioButtonId();
                    if (selectedGenderId == -1) {
                        Toast.makeText(InitialSurveyActivity.this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RadioButton selectedGenderButton = findViewById(selectedGenderId);
                    String gender = selectedGenderButton.getText().toString();

                    // BMI 및 권장 칼로리 계산
                    double heightM = height / 100.0;
                    double bmi = weight / (heightM * heightM);
                    String bmiStatus;
                    if (bmi < 18.5) bmiStatus = "저체중";
                    else if (bmi < 23) bmiStatus = "정상 체중";
                    else if (bmi < 25) bmiStatus = "과체중";
                    else bmiStatus = "비만";

                    String goal;
                    if (bmi >= 25) goal = "lose";
                    else if (bmi < 18.5) goal = "gain";
                    else goal = "maintain";

                    double bmr;
                    if (gender.equals("남성")) {
                        bmr = (10 * weight) + (6.25 * height) - (5 * 25) + 5; // (예시 나이 25세)
                    } else {
                        bmr = (10 * weight) + (6.25 * height) - (5 * 25) - 161; // (예시 나이 25세)
                    }

                    double activityFactor;
                    switch (goal) {
                        case "lose": activityFactor = 1.2; break;
                        case "gain": activityFactor = 1.8; break;
                        default: activityFactor = 1.55; break;
                    }
                    int targetCalories = (int) (bmr * activityFactor);

                    // User 객체 생성 및 저장
                    user = new User();
                    user.setId(UUID.randomUUID().toString());
                    user.setGender(gender);
                    user.setHeight(height);
                    user.setWeight(weight);
                    user.setTargetCalories(targetCalories);

                    //  혈압/당 수치 값 읽기 및 User 객체에 저장
                    String bpSysStr = et_blood_pressure_sys.getText().toString().trim();
                    String bpDiaStr = et_blood_pressure_dia.getText().toString().trim();
                    String bloodSugarStr = et_blood_sugar.getText().toString().trim();

                    // TextUtils.isEmpty로 체크 (null 방지)
                    double bpSys = !TextUtils.isEmpty(bpSysStr) ? Double.parseDouble(bpSysStr) : 0;
                    double bpDia = !TextUtils.isEmpty(bpDiaStr) ? Double.parseDouble(bpDiaStr) : 0;
                    double bloodSugar = !TextUtils.isEmpty(bloodSugarStr) ? Double.parseDouble(bloodSugarStr) : 0;

                    user.setBloodPressure(bpSys, bpDia);
                    user.setBloodSugar(bloodSugar);

                    // SharedPreferences에 User 저장
                    UserPreferenceManager.saveUser(InitialSurveyActivity.this, user);
                    // 오늘의 식단/누적값 초기화
                    UserPreferenceManager.clearTodayData(InitialSurveyActivity.this);

                    //  결과 텍스트 생성 (BMI + 건강 상태)
                    String goalText;
                    int color;
                    switch (goal) {
                        case "lose": goalText = "감량 추천"; color = Color.parseColor("#E53935"); break;
                        case "gain": goalText = "증량 추천"; color = Color.parseColor("#1E88E5"); break;
                        default: goalText = "유지 권장"; color = Color.parseColor("#388E3C"); break;
                    }

                    // BMI 결과 텍스트
                    String bmiResultText = "현재 " + bmiStatus + " (" + String.format("%.1f", bmi) + ")\n" +
                            "목표: " + goalText + " / 권장 섭취: " + targetCalories + " kcal";

                    //  건강 상태 판별 로직 추가
                    String healthStatusText = "";
                    String bpStatus = getBloodPressureStatus(bpSys, bpDia);
                    String sugarStatus = getBloodSugarStatus(bloodSugar); // 공복 혈당 기준

                    if (!bpStatus.isEmpty()) {
                        healthStatusText += "\n혈압: " + bpStatus;
                    }
                    if (!sugarStatus.isEmpty()) {
                        healthStatusText += "\n혈당: " + sugarStatus;
                    }

                    //  결과 표시 및 버튼 텍스트 변경
                    tv_bmi_result.setText(bmiResultText + healthStatusText); //  합쳐서 표시
                    tv_bmi_result.setTextColor(color); // BMI 기준 색상 유지
                    tv_bmi_result.setVisibility(View.VISIBLE); // 결과 창 보이게

                    btn_start.setText("결과 확인 후 시작하기");
                    isResultShown = true;

                } else {
                    //  두 번째 클릭 → MainActivity 이동
                    Intent intent = new Intent(InitialSurveyActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    /**
     * ⭐️ 혈압 상태 판별 헬퍼 메소드 ⭐️
     * (대한고혈압학회 2022년 기준 간략)
     * @param sys 수축기 (높은 값)
     * @param dia 이완기 (낮은 값)
     * @return 상태 문자열
     */
    private String getBloodPressureStatus(double sys, double dia) {
        if (sys == 0 || dia == 0) {
            return ""; // 입력 안 함
        }
        if (sys >= 140 || dia >= 90) {
            return "고혈압 (주의)";
        }
        if (sys >= 130 || dia >= 85) {
            return "고혈압 전단계 (주의)";
        }
        if (sys < 90 || dia < 60) {
            return "저혈압 (주의)";
        }
        return "정상";
    }

    /**
     * ⭐️ 공복 혈당 상태 판별 헬퍼 메소드 ⭐️
     * (대한당뇨병학회 기준)
     * @param sugar 공복 혈당
     * @return 상태 문자열
     */
    private String getBloodSugarStatus(double sugar) {
        if (sugar == 0) {
            return ""; // 입력 안 함
        }
        if (sugar >= 126) {
            return "당뇨병 (위험)";
        }
        if (sugar >= 100) {
            return "당뇨 전단계 (주의)";
        }
        if (sugar < 70) {
            return "저혈당 (주의)";
        }
        return "정상";
    }
}

