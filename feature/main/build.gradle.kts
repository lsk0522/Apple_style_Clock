plugins {
    alias(libs.plugins.nightstand.android.feature)
}

android {
    namespace = "com.lsk0522.nightstand.feature.main"
}

dependencies {
    // The setup screen asks for POST_NOTIFICATIONS through a result launcher.
    implementation(libs.androidx.activity.compose)
}
