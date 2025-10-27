plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.aifoodtracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aifoodtracker"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // 기본 라이브러리들
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- 여기부터 우리가 추가할 라이브러리 ---

    // Retrofit (네트워크 통신)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide (이미지 로딩)
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // MPAndroidChart (그래프/차트)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // 🚨 네이버 지도 SDK 추가
    implementation("com.naver.maps:map-sdk:3.21.0")

    // 🚨 구글 지도/위치 라이브러리 삭제됨
    // implementation("com.google.android.gms:play-services-location:21.0.1")
    // implementation("com.google.android.gms:play-services-maps:18.1.0")
}

