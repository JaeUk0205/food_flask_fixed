package com.example.aifoodtracker;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aifoodtracker.domain.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID; // <<<--- 1. 이 import 구문을 추가합니다.

public class InitialSurveyActivity extends AppCompatActivity {

    private RadioGroup rg_gender;
    private EditText et_height, et_weight;
    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 카카오맵 키 해시를 확인하기 위한 임시 코드 ---
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // --- 여기까지 임시 코드 ---

        setContentView(R.layout.activity_initial_survey);

        rg_gender = findViewById(R.id.rg_gender);
        et_height = findViewById(R.id.et_height);
        et_weight = findViewById(R.id.et_weight);
        btn_start = findViewById(R.id.btn_start);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String heightStr = et_height.getText().toString();
                String weightStr = et_weight.getText().toString();

                if (heightStr.isEmpty() || weightStr.isEmpty()) {
                    Toast.makeText(InitialSurveyActivity.this, "키와 몸무게를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = new User();

                // --- 2. 랜덤 고유 ID 생성 및 설정 ---
                String uniqueID = UUID.randomUUID().toString();
                user.setId(uniqueID);

                int selectedGenderId = rg_gender.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedGenderId);
                user.setGender(selectedRadioButton.getText().toString());
                user.setHeight(Double.parseDouble(heightStr));
                user.setWeight(Double.parseDouble(weightStr));

                int targetCalories;
                if (user.getGender().equals("남성")) {
                    targetCalories = (int) ((10 * user.getWeight()) + (6.25 * user.getHeight()) - (5 * 25) + 5);
                } else {
                    targetCalories = (int) ((10 * user.getWeight()) + (6.25 * user.getHeight()) - (5 * 25) - 161);
                }
                user.setTargetCalories(targetCalories);

                Intent intent = new Intent(InitialSurveyActivity.this, CameraActivity.class);
                intent.putExtra("user_data", user);
                startActivity(intent);
                finish();
            }
        });
    }
}