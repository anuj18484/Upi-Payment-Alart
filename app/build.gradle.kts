
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "in.anuj.upisoundbox"
    compileSdk = 35

    defaultConfig {
        applicationId = "in.anuj.upisoundbox"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
