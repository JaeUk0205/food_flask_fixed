package com.example.aifoodtracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aifoodtracker.domain.User;
import com.example.aifoodtracker.utils.UserPreferenceManager; // ✅ UserPreferenceManager import 추가

import java.util.UUID;

public class InitialSurveyActivity extends AppCompatActivity {

    private RadioGroup rg_gender;
    private EditText et_height, et_weight;
    private Button btn_start;
    private TextView tv_bmi_result;

    private boolean isResultShown = false; // ✅ 첫 클릭 체크용
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_survey);

        rg_gender = findViewById(R.id.rg_gender);
        et_height = findViewById(R.id.et_height);
        et_weight = findViewById(R.id.et_weight);
        btn_start = findViewById(R.id.btn_start);
        tv_bmi_result = findViewById(R.id.tv_bmi_result);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isResultShown) {
                    // ✅ 첫 번째 클릭: BMI 계산 후 결과 표시
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
                        bmr = (10 * weight) + (6.25 * height) - (5 * 25) + 5;
                    } else {
                        bmr = (10 * weight) + (6.25 * height) - (5 * 25) - 161;
                    }

                    double activityFactor;
                    switch (goal) {
                        case "lose": activityFactor = 1.2; break;
                        case "gain": activityFactor = 1.8; break;
                        default: activityFactor = 1.55; break;
                    }

                    int targetCalories = (int) (bmr * activityFactor);

                    String goalText;
                    int color;
                    switch (goal) {
                        case "lose":
                            goalText = "감량 추천";
                            color = Color.parseColor("#E53935"); // 빨강
                            break;
                        case "gain":
                            goalText = "증량 추천";
                            color = Color.parseColor("#1E88E5"); // 파랑
                            break;
                        default:
                            goalText = "유지 권장";
                            color = Color.parseColor("#388E3C"); // 초록
                            break;
                    }

                    // ✅ 결과 표시 + 색상 반영
                    tv_bmi_result.setText(
                            "현재 " + bmiStatus + " (" + String.format("%.1f", bmi) + ")\n" +
                                    "목표: " + goalText + " / 권장 섭취: " + targetCalories + " kcal"
                    );
                    tv_bmi_result.setTextColor(color);

                    // ✅ User 저장
                    user = new User();
                    user.setId(UUID.randomUUID().toString());
                    user.setGender(gender);
                    user.setHeight(height);
                    user.setWeight(weight);
                    user.setTargetCalories(targetCalories);

                    // ✅ User 정보를 SharedPreferences에 저장
                    UserPreferenceManager.saveUser(InitialSurveyActivity.this, user);

                    // ✅ 버튼 텍스트 변경
                    btn_start.setText("결과 확인 후 시작하기");
                    isResultShown = true;

                } else {
                    // 🚨 수정된 부분: 두 번째 클릭 → CameraActivity로 이동
                    Intent intent = new Intent(InitialSurveyActivity.this, CameraActivity.class);
                    intent.putExtra("user_data", user);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
