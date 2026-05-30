plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

ksp {
    arg("room.generateKotlin", "true")    // Room emite código Kotlin puro
    arg("useK2", "true")                  // compilador K2
}

android {
    namespace = "com.example.demodata"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.demodata"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")

    // ── LÍNEA DE FUENTES (Esta arreglará los errores rojos de tu Type.kt) ──
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.0")

    // ── Las líneas que te pide el documento del Laboratorio ──
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    // ── Room (SQLite) ──
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)          // extensiones suspend + Flow
    ksp(libs.androidx.room.compiler)                 // generador de código en tiempo de compilación
    // ── DataStore (reemplaza SharedPreferences) ──
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    // ── ViewModel + lifecycle ──
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    // ── Google Fused Location Provider + bridge de coroutines ──
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.11.0")  // .await()
    // ── Coil (thumbnails de archivos en ProfileScreen → MyActivity) ──
    implementation("io.coil-kt:coil-compose:2.7.0")
    // ── Accompanist: gestión de permisos en runtime con Compose ──
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
}