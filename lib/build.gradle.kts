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
}

// Task 1: JNIヘッダファイルを生成する
val generateJniHeaders by tasks.register<JavaExec>("generateJniHeaders") {
    group = "jni"
    description = "Generates JNI headers from JVM classes."

    // jvmのコンパイルタスクに依存
    dependsOn(tasks.named("jvmJar"))

    // javac -h を実行
    classpath = sourceSets.getByName("jvmMain").output.classesDirs
    mainClass.set("not.used.Main") // mainClassは必須だが使わない
    args = listOf(
        "-h", jniHeaderDir.get().asFile.absolutePath,
        "com.example.NativeLib" // ヘッダを生成するクラス
    )
    executable(File(System.getProperty("java.home"), "bin/javac").path)
}

// Task 2: ネイティブコードをコンパイルする
val compileJniLib by tasks.register<Exec>("compileJniLib") {
    group = "jni"
    description = "Compiles C/C++ source file into a shared library."
    dependsOn(generateJniHeaders)

    val jdkHome = System.getProperty("java.home")
    val sourceFile = "src/jvmMain/c/com_example_NativeLib.c"

    // OSごとにコマンドと引数を切り替える
    val os = org.gradle.internal.os.OperatingSystem.current()
    when {
        os.isWindows -> {
            commandLine(
                "cl.exe",
                "/I", "$jdkHome/include",
                "/I", "$jdkHome/include/win32",
                "/I", jniHeaderDir.get().asFile.path, // 生成したヘッダのパス
                "/LD", // DLLを生成するオプション
                sourceFile,
                "/Fe${layout.buildDirectory}/libs/$nativeLibName.dll" // 出力ファイル名
            )
        }

        os.isLinux -> {
            commandLine(
                "gcc",
                "-shared", "-fPIC",
                "-I$jdkHome/include",
                "-I$jdkHome/include/linux",
                "-I${jniHeaderDir.get().asFile.path}",
                sourceFile,
                "-o", "${layout.buildDirectory}/libs/lib$nativeLibName.so" // 出力ファイル名 (libプレフィックス)
            )
        }

        os.isMacOsX -> {
            commandLine(
                "clang",
                "-shared",
                "-I$jdkHome/include",
                "-I$jdkHome/include/darwin",
                "-I${jniHeaderDir.get().asFile.path}",
                sourceFile,
                "-o", "${layout.buildDirectory}/libs/lib$nativeLibName.dylib"
            )
        }
    }
}

// Task 3: ビルドサイクルにフックする
// jvmProcessResourcesタスクが実行される前に、コンパイルしたライブラリをリソースにコピーする
tasks.named("jvmProcessResources") {
    dependsOn(compileJniLib)
    val os = org.gradle.internal.os.OperatingSystem.current()
    // コンパイル成果物の場所
    val libDir = layout.buildDirectory.dir("libs")
    from(libDir) {
        // lib/os-arch/libname.ext のような形でJARに含める
        val platformDir = when {
            os.isWindows -> "win-x86_64"
            os.isLinux -> "linux-x86_64"
            os.isMacOsX -> "macos-x86_64" // or macos-aarch64
            else -> "unknown"
        }
        // 拡張子もOSによって異なる
        val libFilePattern = when {
            os.isWindows -> "$nativeLibName.dll"
            os.isLinux -> "lib$nativeLibName.so"
            os.isMacOsX -> "lib$nativeLibName.dylib"
            else -> ""
        }
        include(libFilePattern)
        into("lib/$platformDir")
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
