package com.example.aifoodtracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.aifoodtracker.domain.FoodResponse;
import com.example.aifoodtracker.domain.User;
import com.example.aifoodtracker.network.RetrofitAPI;
import com.example.aifoodtracker.network.RetrofitClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private ImageView iv_preview;
    private Button btn_take_picture;
    private Uri photoUri;
    private File imageFile;
    private User user;
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && photoUri != null) {
                    Glide.with(this).load(photoUri).into(iv_preview);
                    Toast.makeText(this, "사진 촬영 성공! 서버로 업로드합니다.", Toast.LENGTH_SHORT).show();
                    uploadImageToServer();
                } else {
                    Toast.makeText(this, "사진 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        iv_preview = findViewById(R.id.iv_preview);
        btn_take_picture = findViewById(R.id.btn_take_picture);

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user_data");

        if (user == null) {
            Toast.makeText(this, "사용자 정보가 없어 앱을 종료합니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btn_take_picture.setOnClickListener(v -> dispatchTakePictureIntent());
    }

    private void dispatchTakePictureIntent() {
        try {
            imageFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "사진 파일 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageFile != null) {
            photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.aifoodtracker.provider",
                    imageFile
            );
            takePictureLauncher.launch(photoUri);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void uploadImageToServer() {
        if (imageFile == null) {
            Toast.makeText(this, "이미지 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("AI가 분석 중입니다...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        RetrofitAPI apiService = RetrofitClient.getApiService();

        apiService.uploadImage(body).enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(@NonNull Call<FoodResponse> call, @NonNull Response<FoodResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    FoodResponse foodResponse = response.body();
                    Toast.makeText(CameraActivity.this, "분석 결과: " + foodResponse.getFoodName(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                    intent.putExtra("user_data", user);
                    intent.putExtra("food_response", foodResponse);
                    intent.putExtra("captured_image_uri", photoUri.toString());
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(CameraActivity.this, "서버 응답 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FoodResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CameraActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}