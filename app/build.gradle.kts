plugins {
    id("com.android.application") version "9.0.1" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
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

dependencies {
    implementation("androidx.room:room-runtime:2.8.2")
    implementation("androidx.room:room-ktx:2.8.2")
    ksp("androidx.room:room-compiler:2.8.2")
}
