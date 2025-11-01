package com.example.aifoodtracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import com.example.aifoodtracker.utils.UserPreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private static final String TAG = "CameraActivity"; // 로그 태그

    private ImageView iv_preview;
    private Button btn_take_picture, btn_select_gallery; // 갤러리 버튼 변수
    private Uri photoUri; // 카메라 촬영 원본 URI
    private File imageFile; // 서버로 전송할 파일
    private User user;
    private ProgressDialog progressDialog;

    // 카메라 앱 런처
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && photoUri != null) {
                    Glide.with(this).load(photoUri).into(iv_preview);
                    imageFile = new File(photoUri.getPath()); // photoUri로부터 File 객체 생성 (경로 확인 필요)
                    Toast.makeText(this, "사진 촬영 성공! 서버로 업로드합니다.", Toast.LENGTH_SHORT).show();
                    uploadImageToServer();
                } else {
                    Log.e(TAG, "사진 촬영 취소 또는 photoUri가 null입니다.");
                    Toast.makeText(this, "사진 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // 갤러리 런처
    private final ActivityResultLauncher<String> selectGalleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        // 갤러리에서 선택한 이미지(uri)를 앱 내부 저장소의 임시 파일(imageFile)로 복사
                        imageFile = createImageFile(); // 새 임시 파일 생성
                        copyUriToFile(uri, imageFile); // URI 내용을 파일에 복사

                        photoUri = FileProvider.getUriForFile(
                                this,
                                "com.example.aifoodtracker.provider",
                                imageFile
                        ); // 파일로부터 FileProvider URI 생성 (썸네일 표시 및 전달용)

                        Glide.with(this).load(uri).into(iv_preview); // 프리뷰에는 원본 uri 로드
                        Toast.makeText(this, "갤러리 선택 성공! 서버로 업로드합니다.", Toast.LENGTH_SHORT).show();
                        uploadImageToServer();

                    } catch (IOException e) {
                        Log.e(TAG, "갤러리 이미지 파일 복사 실패", e);
                        Toast.makeText(this, "파일 처리에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        imageFile = null;
                        photoUri = null;
                    }
                } else {
                    Log.e(TAG, "갤러리 선택 취소 또는 uri가 null입니다.");
                    Toast.makeText(this, "갤러리 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        iv_preview = findViewById(R.id.iv_preview);
        btn_take_picture = findViewById(R.id.btn_take_picture);
        btn_select_gallery = findViewById(R.id.btn_select_gallery); // 갤러리 버튼 ID 연결

        // ⭐️⭐️⭐️ 수정: Intent 대신 SharedPreferences에서 User 정보 로드 ⭐️⭐️⭐️
        // user = intent.getParcelableExtra("user_data"); // ⭐️ 이 줄 삭제!
        user = UserPreferenceManager.getUser(this); // ⭐️ SharedPreferences에서 로드

        if (user == null) {
            Toast.makeText(this, "사용자 정보가 없어 앱을 종료합니다.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "User 정보 로드 실패, CameraActivity 종료");
            finish();
            return;
        }

        // '사진 촬영' 버튼 클릭 리스너
        btn_take_picture.setOnClickListener(v -> dispatchTakePictureIntent());

        // '갤러리 선택' 버튼 클릭 리스너
        btn_select_gallery.setOnClickListener(v -> dispatchSelectGalleryIntent());
    }

    // 카메라 앱 실행
    private void dispatchTakePictureIntent() {
        try {
            imageFile = createImageFile(); // 서버 전송용 파일 생성
        } catch (IOException ex) {
            Log.e(TAG, "사진 파일 생성 실패", ex);
            Toast.makeText(this, "사진 파일 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageFile != null) {
            photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.aifoodtracker.provider",
                    imageFile
            ); // FileProvider를 통해 카메라 앱이 접근 가능한 URI 생성
            Log.d(TAG, "카메라 호출, photoUri: " + photoUri);
            takePictureLauncher.launch(photoUri); // 카메라 앱 실행
        }
    }

    // 갤러리 앱 실행
    private void dispatchSelectGalleryIntent() {
        Log.d(TAG, "갤러리 호출");
        selectGalleryLauncher.launch("image/*"); // 모든 이미지 타입 갤러리 열기
    }

    // (공통) 이미지 파일 생성 (앱 내부 저장소)
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); // 앱 전용 외부 저장소
        // File storageDir = getCacheDir(); // 앱 내부 캐시 디렉토리 (더 좋음)
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.d(TAG, "임시 파일 생성: " + image.getAbsolutePath());
        return image;
    }

    // (갤러리용) 갤러리 URI(InputStream)를 앱 내부 파일(OutputStream)로 복사
    private void copyUriToFile(Uri uri, File file) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            outputStream = new FileOutputStream(file);
            if (inputStream == null) {
                throw new IOException("InputStream is null");
            }
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            Log.d(TAG, "갤러리 파일 복사 완료: " + file.length() + " bytes");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }


    // (공통) 서버로 이미지 업로드
    private void uploadImageToServer() {
        if (imageFile == null || !imageFile.exists() || imageFile.length() == 0) {
            Log.e(TAG, "업로드할 이미지 파일이 없거나 유효하지 않습니다.");
            Toast.makeText(this, "업로드할 이미지 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "서버 업로드 시작: " + imageFile.getName() + " (" + imageFile.length() + " bytes)");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("AI가 분석 중입니다...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // ❗️ 갤러리 파일 MIME 타입 추론 (필요시)
        // MediaType.parse("image/jpeg") 대신 getContentResolver().getType(uri) 사용 가능
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        RetrofitAPI apiService = RetrofitClient.getApiService();

        apiService.uploadImage(body).enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(@NonNull Call<FoodResponse> call, @NonNull Response<FoodResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    FoodResponse foodResponse = response.body();
                    Log.d(TAG, "서버 분석 성공: " + foodResponse.getFoodName());
                    Toast.makeText(CameraActivity.this, "분석 결과: " + foodResponse.getFoodName(), Toast.LENGTH_SHORT).show();

                    // ✅ 결과를 MainActivity로 전달
                    Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                    // ⭐️⭐️⭐️ 수정: user_data를 Intent로 넘기지 않음! ⭐️⭐️⭐️
                    // intent.putExtra("user_data", user); // ⭐️ 이 줄 삭제! (오류 발생 지점)
                    intent.putExtra("food_response", foodResponse);

                    // ⭐️ photoUri가 null이 아닐 경우에만 전달 (갤러리 선택 시 photoUri가 FileProvider URI임)
                    if (photoUri != null) {
                        intent.putExtra("captured_image_uri", photoUri.toString());
                    } else {
                        // 갤러리 선택 시 썸네일로 쓸 URI가 필요하면, 갤러리 원본 uri를 전달해야 함
                        // (현재 로직에서는 photoUri가 생성되므로 이 case는 거의 없음)
                        Log.w(TAG, "photoUri가 null이라 MainActivity로 이미지 URI를 전달하지 못했습니다.");
                    }

                    // ⭐️ onNewIntent를 사용하기 위해 Flag 추가
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent);
                    finish(); // 현재 CameraActivity 종료

                } else {
                    Log.e(TAG, "서버 응답 실패: " + response.code() + " - " + response.message());
                    try {
                        Log.e(TAG, "서버 오류 바디: " + (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (IOException e) {
                        Log.e(TAG, "오류 바디 읽기 실패", e);
                    }
                    Toast.makeText(CameraActivity.this, "서버 응답 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FoodResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "네트워크 오류: " + t.getMessage(), t);
                Toast.makeText(CameraActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

