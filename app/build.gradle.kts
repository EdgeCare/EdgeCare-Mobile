import org.gradle.kotlin.dsl.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("io.objectbox")
}

android {
    namespace = "com.example.edgecare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.edgecare"
        minSdk = 28
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(files("libs/onnxruntime-android-1.17.3.aar"))
    implementation(libs.objectBoxKotlin)
    implementation (libs.recyclerview)
    implementation (libs.cardView)
    implementation (libs.gson)
    implementation (libs.androidx.viewpager2)
    testImplementation(libs.junit)
    implementation (libs.itextpdf)
    implementation (libs.retrofit)
    implementation (libs.retrofitGson)
}