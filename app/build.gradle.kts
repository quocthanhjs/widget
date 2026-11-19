plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "vn.dqt.lunarweather"
    compileSdk = 35
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    defaultConfig {
        applicationId = "vn.dqt.lunarweather"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "3.0.0"
    }
}
