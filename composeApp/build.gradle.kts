import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.mupdfKmp.detekt)
    alias(libs.plugins.mupdfKmp.gitTagVersion)
    alias(libs.plugins.mupdfKmp.lint)
}

kotlin {
    android {
        namespace = "com.sorrowblue.mupdf.kmp.shared"
        androidResources.enable = true
    }
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

compose.desktop {
    application {
        mainClass = "com.sorrowblue.mupdf.kmp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.sorrowblue.mupdf.kmp"
            packageVersion = "1.0.0"

            linux {
                modules("jdk.security.auth")
            }
        }
    }
}
