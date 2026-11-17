plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ulak.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ulak.app"
        // 26 (Android 8.0) lets mesh-core use java.time without extra setup.
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        // Robolectric tests need the app's real resources (strings, theme).
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    implementation("com.google.android.gms:play-services-nearby:19.5.1")

    implementation(project(":mesh-core"))

    // Tests only: run Android code and Compose UI on the computer, no phone needed.
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.18")
    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
