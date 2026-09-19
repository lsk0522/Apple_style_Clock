plugins {
    alias(libs.plugins.nightstand.android.library)
    alias(libs.plugins.nightstand.android.compose)
}

android {
    namespace = "com.lsk0522.nightstand.core.design"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // api: screens need HazeState to mark their own scrolling content as the
    // blur source, so the type has to leak out of this module.
    api(libs.haze)

    testImplementation(libs.junit)
}
