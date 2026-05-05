import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.mupdfKmp.detekt)
    alias(libs.plugins.mupdfKmp.lint)
}

kotlin {
    jvm()

    jvmToolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }

    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.shared)
                implementation(projects.lib)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.sorrowblue.mupdf.kmp.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.sorrowblue.mupdf.kmp.app"
            packageVersion = "1.0.0"

            linux {
                modules("jdk.security.auth")
            }
        }
    }
}
