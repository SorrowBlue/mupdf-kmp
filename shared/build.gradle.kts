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
    jvm()

    jvmToolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.componentsResources)
                implementation(libs.compose.preview)
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
                implementation(libs.androidx.activity.compose)
            }
        }
        jvmMain {
            dependencies {
                implementation(projects.lib)
                implementation(libs.kotlinx.coroutinesSwing)
            }
        }

    }
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
