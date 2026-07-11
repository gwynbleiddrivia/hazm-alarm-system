plugins {
    id("com.android.application")
}

android {
    namespace = "com.dreyfus.hazm"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dreyfus.hazm"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
}
