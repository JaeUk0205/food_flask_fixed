plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.aifoodtracker" // 본인 프로젝트에 맞게 설정됨
    compileSdk = 34 // 이 숫자는 다를 수 있습니다

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
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0" )
    //implementation("at.grabner:circleprogress:1.3")
    //implementation("com.github.hayahyts:Dotted-Progress-Indicator:1.0.1")

    implementation("com.naver.maps:map-sdk:3.21.0")








}



