import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.mupdfKmp.muBuild)
    alias(libs.plugins.mupdfKmp.gitTagVersion)
    alias(libs.plugins.mupdfKmp.detekt)
    alias(libs.plugins.mupdfKmp.lint)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    jvmToolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

java {
    sourceSets.getByName("jvmMain").java.srcDir("../mupdf/platform/java/src")
        .exclude("**/android/**")
}

buildkonfig {
    packageName = "com.sorrowblue.mupdf.kmp"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "version", version.toString())
    }
}

android {
    namespace = "org.sorrowblue.mupdf.kmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    sourceSets.getByName("main").java.srcDir("../mupdf/platform/java/src")

    externalNativeBuild {
        ndkVersion = libs.versions.ndkVersion.get()
        ndkBuild.path("../mupdf/platform/java/Android.mk")
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

mavenPublishing {
    publishToMavenCentral()

    coordinates("com.sorrowblue.mupdf", "mupdf-kmp", version.toString())

    pom {
        name = "mupdf-kmp"
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
