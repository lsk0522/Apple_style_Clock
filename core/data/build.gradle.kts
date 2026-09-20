plugins {
    alias(libs.plugins.nightstand.android.library)
    alias(libs.plugins.nightstand.android.hilt)
}

android {
    namespace = "com.lsk0522.nightstand.core.data"
}

dependencies {
    // api: ChargeType/ChargingStatus appear in this module's public signatures.
    api(projects.core.common)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    api(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
