plugins {
    alias(libs.plugins.nightstand.android.library)
    alias(libs.plugins.nightstand.android.compose)
}

android {
    namespace = "com.lsk0522.nightstand.core.design"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
