plugins {
    alias(libs.plugins.nightstand.android.library)
    alias(libs.plugins.nightstand.android.hilt)
}

android {
    namespace = "com.lsk0522.nightstand.core.data"
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    api(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
