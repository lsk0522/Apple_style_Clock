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
    // KSP and the Compose compiler plugin are applied by id, and AGP 9 brings
    // its own Kotlin, so none of those jars need to be on this classpath --
    // which also keeps their newer Kotlin metadata away from the embedded
    // compiler that builds this module.
    compileOnly(libs.android.gradlePlugin)
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
