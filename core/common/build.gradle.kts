plugins {
    alias(libs.plugins.nightstand.android.library)
}

android {
    namespace = "com.lsk0522.nightstand.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
