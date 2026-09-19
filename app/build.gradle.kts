plugins {
    alias(libs.plugins.nightstand.android.application)
    alias(libs.plugins.nightstand.android.compose)
    alias(libs.plugins.nightstand.android.hilt)
}

// Every CI build gets a higher versionCode than the one before it, so a freshly
// downloaded APK installs *over* the copy already on the phone. Local builds
// fall back to 1.
val buildNumber = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .orNull?.toIntOrNull() ?: 1

android {
    namespace = "com.lsk0522.nightstand"

    defaultConfig {
        applicationId = "com.lsk0522.nightstand"
        versionCode = buildNumber
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // A checked-in debug key. CI runners generate a throwaway debug keystore
        // on every run, which changes the signature each build and makes Android
        // refuse the install ("app not installed"). Pinning the key here is what
        // turns each new APK into a plain update.
        //
        // This key is deliberately public and signs debug builds only; the
        // release key is created separately in Phase 11 and never committed.
        getByName("debug") {
            storeFile = rootProject.file("signing/debug.jks")
            storePassword = "nightstand"
            keyAlias = "nightstand-debug"
            keyPassword = "nightstand"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug.$buildNumber"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // TODO(next): Phase 11 — wire release signing from keystore.properties
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.core.design)
    implementation(projects.core.common)
    implementation(projects.core.data)

    implementation(projects.feature.main)
    implementation(projects.feature.standby)
    implementation(projects.feature.widgets)
    implementation(projects.feature.charging)
    implementation(projects.feature.developer)
    implementation(projects.feature.donate)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
