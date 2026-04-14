plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.gymtracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gymtracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 7
        versionName = "1.6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Room Database
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Gson for JSON export/import
    implementation("com.google.code.gson:gson:2.10.1")

    // Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // Glance for widgets
    implementation("androidx.glance:glance-appwidget:1.0.0")

    // Work Manager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // NanoHTTPD — embedded HTTP server for watch sync
    implementation("org.nanohttpd:nanohttpd:2.3.1")

    // OpenStreetMap — no API key required
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Device location (FusedLocationProviderClient) — no API key required
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Accompanist Permissions (runtime location permission dialog)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // OkHttp — Overpass API calls (no API key required)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Coroutines + Play Services (for Task.await())
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
