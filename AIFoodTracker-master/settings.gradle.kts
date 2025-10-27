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
    }
}

rootProject.name = "AIFoodTracker"
include(":app")