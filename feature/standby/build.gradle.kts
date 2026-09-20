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

    // The dream hosts Compose from a Service, which has none of the owners an
    // Activity provides for free — it has to supply its own saved-state
    // registry or ComposeView refuses to attach.
    implementation(libs.androidx.savedstate)
}
