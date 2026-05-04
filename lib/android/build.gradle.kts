plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.mupdfKmp.detekt)
    alias(libs.plugins.mupdfKmp.gitTagVersion)
    alias(libs.plugins.mupdfKmp.lint)
}

kotlin {
    jvmToolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

android {
    namespace = "com.sorrowblue.mupdf.kmp.android"
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    sourceSets {
        named("main") {
            java.directories.add("../../mupdf/platform/java/src")
        }
    }
    externalNativeBuild {
        ndkVersion = libs.versions.ndkVersion.get()
        ndkBuild.path("../../mupdf/platform/java/Android.mk")
    }
    lint {
        lintConfig = file("lint.xml")
    }
}

mavenPublishing {
    publishToMavenCentral()

    coordinates("com.sorrowblue.mupdf", "mupdf-android", version.toString())

    pom {
        name = "mupdf-android"
        description = "Use MuPDF with KotlinMultiplatform"
        inceptionYear = "2025"
        url = "https://github.com/SorrowBlue/mupdf-kmp"
        licenses {
            license {
                name = "GNU Affero General Public License version 3.0"
                url = "https://www.gnu.org/licenses/agpl-3.0.html"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "sorrowblue"
                name = "Sorrow Blue"
                url = "https://github.com/SorrowBlue"
            }
        }
        scm {
            url = "https://github.com/SorrowBlue/mupdf-kmp"
            connection = "scm:git:https://github.com/SorrowBlue/mupdf-kmp.git"
            developerConnection = "scm:git:ssh://git@github.com/SorrowBlue/mupdf-kmp.git"
        }
    }
}
