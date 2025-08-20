import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.muBuild)
    alias(libs.plugins.gitTagVersion)
    alias(libs.plugins.mavenPublish)
    id("com.codingfeline.buildkonfig") version "0.17.1"
}

group = "com.sorrowblue.mupdf.kmp"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "ComposeApp"
//            isStatic = true
//        }
//    }

    jvm()

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        outputModuleName.set("lib")
//        browser {
//            val rootDirPath = project.rootDir.path
//            val projectDirPath = project.projectDir.path
//            commonWebpackConfig {
//                outputFileName = "lib.js"
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
//                        add(rootDirPath)
//                        add(projectDirPath)
//                    }
//                }
//            }
//        }
//        binaries.executable()
//    }

    jvmToolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
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

    afterEvaluate {
        // com.sorrowblue.mupdf:mupdf-kmp:1.0.0
        coordinates(
            groupId = "com.sorrowblue.mupdf",
            artifactId = "mupdf-kmp",
            version = version.toString()
        )
        logger.lifecycle("publish $version")
    }

    pom {
        afterEvaluate {
            this@pom.name.set("mupdf-kmp")
        }
        description.set(
            "Use MuPDF with KotlinMultiplatform"
        )
        inceptionYear.set("2025")
        url.set("https://github.com/SorrowBlue/mupdf-kmp")
        licenses {
            license {
                name.set("GNU Affero General Public License version 3.0")
                url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("sorrowblue")
                name.set("Sorrow Blue")
                url.set("https://github.com/SorrowBlue")
            }
        }
        scm {
            url.set("https://github.com/SorrowBlue/mupdf-kmp")
            connection.set("scm:git:https://github.com/SorrowBlue/mupdf-kmp.git")
            developerConnection.set(
                "scm:git:ssh://git@github.com/SorrowBlue/mupdf-kmp.git"
            )
        }
    }
}
