plugins {
    alias(libs.plugins.nightstand.android.feature)
}

android {
    namespace = "com.lsk0522.nightstand.feature.main"
}

dependencies {
    // The widgets tab is that feature's own screen; the shell only hosts it.
    implementation(projects.feature.widgets)

    // The setup screen asks for POST_NOTIFICATIONS through a result launcher.
    implementation(libs.androidx.activity.compose)
}
