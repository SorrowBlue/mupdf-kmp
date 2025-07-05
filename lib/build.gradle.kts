import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.security.MessageDigest
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.muBuild)
    id("maven-publish")
    id("com.codingfeline.buildkonfig") version "0.17.1"
}

group = "com.sorrowblue.mupdf.kmp"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("lib")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "lib.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
}

java.sourceSets.forEach {
    it.java.srcDir("../mupdf/platform/java/src").exclude("**/android/**")
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

    externalNativeBuild {
        ndkBuild.path("../mupdf/platform/java/Android.mk")
    }
}

// --- 配布設定 ---
// KMPでは、ターゲットごとにPublicationが自動生成される
// 例: "jvm", "kotlinMultiplatform", "metadata" など
// そのため、リポジトリを指定するだけで済む場合が多い
publishing {
    repositories {
        mavenLocal() // ローカルのMavenリポジトリ (~/.m2/repository)
    }

    // (オプション) Publicationのカスタマイズ
    // KMPのJVMターゲットのアーティファクトIDは、デフォルトで "[プロジェクト名]-jvm" になる
    // これを変更したい場合などにカスタマイズする
    publications.withType<MavenPublication>().configureEach {
        // POM情報をカスタマイズ
        pom {
            name.set("My KMP JNI Library")
            description.set("A KMP library with JNI support for JVM.")
            // ... (licenses, developers, scmなど)
        }
    }
}
