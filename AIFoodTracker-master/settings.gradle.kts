pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 🚨 네이버 저장소 주석 처리
        // maven { url = uri("https://repo.naver.com/naver") }
        maven { url = uri("https://jitpack.io") } // Jitpack은 남겨둡니다 (MPAndroidChart 때문)
    }
}
rootProject.name = "AIFoodTracker"
include(":app")

