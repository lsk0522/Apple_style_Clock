plugins {
    alias(libs.plugins.nightstand.android.application)
    alias(libs.plugins.nightstand.android.compose)
    alias(libs.plugins.nightstand.android.hilt)
}

android {
    namespace = "com.lsk0522.nightstand"

    defaultConfig {
        applicationId = "com.lsk0522.nightstand"
        versionCode = 1
        versionName = "0.1.0-phase0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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
