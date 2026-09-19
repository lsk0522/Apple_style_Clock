pluginManagement {
    // Convention plugins live in build-logic/ and are consumed as an included build.
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nightstand"

// Lets modules refer to each other as `projects.core.design` instead of a
// stringly-typed path. Still an incubating feature in Gradle 9, so it has to
// be opted into here.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// Core — shared foundations
include(":core:design")
include(":core:common")
include(":core:data")

// Features — one per bottom-tab / major surface
include(":feature:main")
include(":feature:standby")
include(":feature:widgets")
include(":feature:charging")
include(":feature:developer")
include(":feature:donate")
