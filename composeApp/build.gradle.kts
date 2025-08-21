import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.mupdfKmp.detekt)
    alias(libs.plugins.mupdfKmp.gitTagVersion)
    alias(libs.plugins.mupdfKmp.lint)
}

kotlin {
    androidTarget()

    jvm("desktop")

    jvmToolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.filekit.dialogsCompose)
            }
        }
        androidMain {
            dependencies {
                logger.lifecycle(System.getProperty("os.name"))
                if (System.getProperty("os.name").lowercase().contains("windows")) {
                    implementation("com.sorrowblue.mupdf:mupdf-kmp:$version")
                } else {
                    implementation(projects.lib)
                }
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
            }
        }
        getByName("desktopMain") {
            dependencies {
                implementation(projects.lib)
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
            }
        }

    }
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "org.sorrowblue.mupdf.kmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.sorrowblue.mupdf.kmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.sorrowblue.mupdf.kmp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.sorrowblue.mupdf.kmp"
            packageVersion = "1.0.0"

            linux {
                modules("jdk.security.auth")
            }
        }
    }
}
