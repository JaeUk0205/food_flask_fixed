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
        // MPAndroidChart, CircleProgressView, DottedProgressBar 등
        maven("https://jitpack.io")
        // 카카오맵

        // 네이버 지도
        maven("https://repository.map.naver.com/archive/maven")
    }
}

rootProject.name = "AIFoodTracker"
include(":app")