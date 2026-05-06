import java.net.URI

rootProject.name = "mupdf-kmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = URI("https://central.sonatype.com/repository/maven-snapshots/")
        }
        mavenLocal()
    }
}

android {
    compileSdk = 37
    minSdk = 30
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.android.settings") version "9.2.1"
}

include(":androidApp")
include(":desktopApp")
include(":lib")
include(":lib:android")
include(":shared")
