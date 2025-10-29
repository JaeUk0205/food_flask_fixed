package com.example.aifoodtracker;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore; // MediaStore import 추가
import android.util.Log; // Log import 추가
import android.webkit.MimeTypeMap; // MimeTypeMap import 추가
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
import java.io.FileOutputStream; // FileOutputStream import 추가
import java.io.IOException;
import java.io.InputStream; // InputStream import 추가
import java.io.OutputStream; // OutputStream import 추가
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

    private static final String TAG = "CameraActivity"; // 로그 태그 추가

    private ImageView iv_preview;
    private Button btn_take_picture;
    private Button btn_select_gallery; // 갤러리 버튼 변수 추가
    private Uri photoUri; // 카메라 촬영 결과 URI
    private File imageFile; // 서버에 업로드할 최종 파일
    private User user;
    private ProgressDialog progressDialog;

    // 카메라 앱 실행 결과 처리 런처
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            isSuccess -> {
                if (isSuccess && photoUri != null) {
                    // imageFile은 createImageFile()에서 이미 생성되었음
                    Glide.with(this).load(photoUri).into(iv_preview);
                    Toast.makeText(this, "사진 촬영 성공! 서버로 업로드합니다.", Toast.LENGTH_SHORT).show();
                    uploadImageToServer();
                } else {
                    Toast.makeText(this, "사진 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // 🚨 갤러리 앱 실행 결과 처리 런처 (새로 추가)
    private final ActivityResultLauncher<String> selectPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    photoUri = uri; // 갤러리에서 선택된 이미지 URI 저장
                    Glide.with(this).load(photoUri).into(iv_preview);
                    Toast.makeText(this, "사진 선택 성공! 서버로 업로드합니다.", Toast.LENGTH_SHORT).show();
                    // 갤러리 URI를 서버 업로드용 File 객체로 변환
                    imageFile = uriToFile(photoUri);
                    if (imageFile != null) {
                        uploadImageToServer();
                    } else {
                        Toast.makeText(this, "파일 변환에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "사진 선택이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        iv_preview = findViewById(R.id.iv_preview);
        btn_take_picture = findViewById(R.id.btn_take_picture);
        btn_select_gallery = findViewById(R.id.btn_select_gallery); // 갤러리 버튼 연결

        // ... (User 정보 로드 부분 동일) ...
        Intent intent = getIntent();
        user = intent.getParcelableExtra("user_data");
        if (user == null) {
            user = UserPreferenceManager.getUser(this);
        }
        if (user == null) {
            Toast.makeText(this, "사용자 정보가 없어 앱을 종료합니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 버튼 클릭 리스너 설정
        btn_take_picture.setOnClickListener(v -> dispatchTakePictureIntent());
        btn_select_gallery.setOnClickListener(v -> dispatchSelectPictureIntent()); // 갤러리 버튼 리스너 추가
    }

    // 카메라 앱 실행
    private void dispatchTakePictureIntent() {
        try {
            // 서버 업로드용 파일 객체 생성 (이 파일에 카메라 앱이 사진 저장)
            imageFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "사진 파일 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "createImageFile failed", ex); // 로그 추가
            return;
        }

        if (imageFile != null) {
            // FileProvider를 통해 카메라 앱이 접근 가능한 URI 생성
            photoUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider", // authorities 수정
                    imageFile
            );
            takePictureLauncher.launch(photoUri); // 카메라 앱 실행
        }
    }

    // 🚨 갤러리 앱 실행 (새로 추가)
    private void dispatchSelectPictureIntent() {
        // "image/*" 타입의 콘텐츠를 가져오는 인텐트 실행
        selectPictureLauncher.launch("image/*");
    }


    // 이미지 파일 생성 (카메라용)
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            Log.e(TAG, "ExternalFilesDir is null");
            throw new IOException("ExternalFilesDir is null");
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "Created image file: " + image.getAbsolutePath()); // 로그 추가
        return image;
    }

    // 🚨 갤러리 URI를 서버 업로드용 File 객체로 변환 (새로 추가 - 핵심 로직)
    private File uriToFile(Uri uri) {
        File file = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // 임시 파일 생성 (카메라 촬영과 유사하게)
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String tempFileName = "JPEG_" + timeStamp + "_gallery";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                Log.e(TAG, "ExternalFilesDir is null during URI to File conversion");
                return null;
            }
            // 파일 확장자 가져오기 (선택 사항, 없으면 jpg로 고정)
            String extension = ".jpg";
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(uri));
            if (type != null) {
                extension = "." + type;
            }

            file = File.createTempFile(tempFileName, extension, storageDir);
            Log.d(TAG, "Created temp file for gallery image: " + file.getAbsolutePath());

            // ContentResolver를 통해 URI로부터 InputStream 얻기
            inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open InputStream from URI: " + uri);
                return null; // 스트림 열기 실패
            }

            // InputStream의 데이터를 FileOutputStream으로 복사
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            Log.d(TAG, "Successfully copied gallery image to temp file");
            return file; // 성공적으로 복사된 파일 반환

        } catch (IOException e) {
            Log.e(TAG, "Failed to convert URI to File", e);
            // 실패 시 임시 파일 삭제 (선택 사항)
            if (file != null && file.exists()) {
                file.delete();
            }
            return null; // 오류 발생 시 null 반환
        } finally {
            // 스트림 닫기
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams", e);
            }
        }
    }


    // 서버로 이미지 업로드 (기존 코드 재사용)
    private void uploadImageToServer() {
        if (imageFile == null || !imageFile.exists()) { // 파일 존재 여부 체크 추가
            Toast.makeText(this, "업로드할 이미지 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "imageFile is null or does not exist before upload");
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("AI가 분석 중입니다...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 파일 확장자에 따라 MediaType 결정 (선택 사항, jpg로 고정해도 무방)
        String mimeType = "image/jpeg";
        String extension = MimeTypeMap.getFileExtensionFromUrl(imageFile.getAbsolutePath());
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            if (mimeType == null) mimeType = "image/jpeg"; // 기본값
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        Log.d(TAG, "Uploading file: " + imageFile.getName() + ", size: " + imageFile.length() + ", type: " + mimeType); // 로그 추가

        RetrofitAPI apiService = RetrofitClient.getApiService();

        apiService.uploadImage(body).enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(@NonNull Call<FoodResponse> call, @NonNull Response<FoodResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    FoodResponse foodResponse = response.body();
                    Toast.makeText(CameraActivity.this, "분석 결과: " + foodResponse.getFoodName(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Upload successful: " + foodResponse.getFoodName());

                    Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                    intent.putExtra("user_data", user);
                    intent.putExtra("food_response", foodResponse);
                    // 🚨 photoUri는 카메라 촬영 URI 또는 갤러리 URI일 수 있음
                    intent.putExtra("captured_image_uri", photoUri.toString());
                    startActivity(intent);
                    finish();

                } else {
                    String errorBody = "N/A";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(CameraActivity.this, "서버 응답 실패: " + response.code() + " / " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Server response error: " + response.code() + " - " + response.message() + " - Body: " + errorBody);
                }
                // 성공/실패 여부와 관계없이 임시 파일 삭제 (선택 사항)
                // if (imageFile != null && imageFile.exists()) {
                //     imageFile.delete();
                // }
            }

            @Override
            public void onFailure(@NonNull Call<FoodResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(CameraActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network error during upload", t); // 상세 로그 추가
                // 실패 시 임시 파일 삭제 (선택 사항)
                // if (imageFile != null && imageFile.exists()) {
                //     imageFile.delete();
                // }
            }
        });
    }
}
