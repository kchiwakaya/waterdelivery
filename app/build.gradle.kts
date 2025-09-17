plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.balckliquid.h20"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.balckliquid.h20"
        minSdk = 27
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation ("androidx.compose:compose-bom:2024.02.00")

    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.ui:ui-graphics")
    implementation ("androidx.compose.ui:ui-tooling-preview")
    implementation ("androidx.compose.material3:material3:1.1.2")
    implementation ("androidx.compose.material:material:1.5.4")  // For SwipeToDismiss
    implementation("androidx.compose.foundation:foundation:1.5.4")
    // Navigation
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    // Navigation
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    // Firebase
    implementation ("com.google.firebase:firebase-bom:32.7.2")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.10.1")
    implementation ("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    // Material Icons
    implementation ("androidx.compose.material:material-icons-core:1.5.4")
    implementation ("androidx.compose.material:material-icons-extended:1.5.4")


    // Debug
    debugImplementation ("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.5.4")

    // Testing
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.5.4")
}