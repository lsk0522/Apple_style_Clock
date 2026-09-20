plugins {
    alias(libs.plugins.nightstand.android.feature)
}

android {
    namespace = "com.lsk0522.nightstand.feature.main"
}

dependencies {
    // The widgets and donate tabs are those features' own screens; the shell
    // only hosts them.
    implementation(projects.feature.widgets)
    implementation(projects.feature.donate)

    // The setup screen asks for POST_NOTIFICATIONS through a result launcher.
    implementation(libs.androidx.activity.compose)
}
