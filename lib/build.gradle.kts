import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

group = "com.sorrowblue.mupdf.kmp"
version = "1.0.0"


// ネイティブライブラリの名前を定義
val nativeLibName = "mynativelib"
// JNIヘッダの出力先
val jniHeaderDir = layout.buildDirectory.dir("generated/jni")

kotlin {
    androidTarget {
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

    sourceSets {
        commonMain.dependencies {
        }

        val planeJvmMain by creating {

        }
        androidMain {
            dependsOn(planeJvmMain)
            dependencies {

            }
        }
        jvmMain {
            dependsOn(planeJvmMain)
        }
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
