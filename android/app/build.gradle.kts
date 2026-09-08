plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.leviathan.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.leviathan.app"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "0.5.0"

        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"https://leviathan-api-production.up.railway.app\""
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

    packaging {
        resources.excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.08.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.10.0")

    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    implementation("org.signal:libsignal-client:0.102.0")
    implementation("org.signal:libsignal-android:0.102.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
