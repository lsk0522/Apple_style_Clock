plugins {
    alias(libs.plugins.nightstand.android.feature)
}

android {
    namespace = "com.lsk0522.nightstand.feature.widgets"
}

dependencies {
    // The widget picker launches the system's bind-consent prompt for a result.
    implementation(libs.androidx.activity.compose)
}
