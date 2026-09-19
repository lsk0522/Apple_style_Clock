plugins {
    `kotlin-dsl`
}

group = "com.lsk0522.nightstand.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Only plugins whose *types* the convention code touches belong here.
    // KSP and the Compose compiler plugin are applied by id, so adding their
    // jars would just put Kotlin 2.3-metadata classes on a classpath that
    // Gradle compiles with its own embedded Kotlin 2.0 — which cannot read them.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "nightstand.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "nightstand.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "nightstand.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "nightstand.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidFeature") {
            id = "nightstand.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
    }
}
