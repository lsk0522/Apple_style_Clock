plugins {
    alias(libs.plugins.nightstand.android.feature)
}

android {
    namespace = "com.lsk0522.nightstand.feature.standby"
}

dependencies {
    // The StandBy surface renders the hosted widgets; :feature:widgets owns
    // the host and the tile.
    implementation(projects.feature.widgets)
}
