package com.sorrowblue.mupdfkmp.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register

class MsBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Windows x86
            tasks.register<MsBuildTask>("buildJavaviewerlibWindowsX86") {
                group = "build"
                description = "Builds the native javaviewerlib using MSBuild."
                workingDirectory.set(layout.projectDirectory.dir("../mupdf/platform/win32/"))
                projectName.set("javaviewerlib.vcxproj")
                configuration.set("Release")
                platform.set("x86")
            }
            tasks.register<Copy>("copyJavaviewerlibDll") {
                mustRunAfter("buildJavaviewerlibWindowsX86")
                group = "build"
                description = "Copies javaviewerlib64.dll to the resources folder."
                from(file("../mupdf/platform/win32/Release/javaviewerlib.dll"))
                into(layout.projectDirectory.dir("src/jvmMain/resources/Windows-x84"))
                rename { "mupdf_java.dll" }
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
            tasks.register<GenerateMupdfLibPropertiesTask>("generateMupdfLibPropertiesWindowsX86") {
                mustRunAfter("copyJavaviewerlibDll")
                libVersion = version.toString()
                dllFile = layout.projectDirectory.file("src/jvmMain/resources/Windows-x84/mupdf_java.dll")
                propsFile = layout.projectDirectory.file("src/jvmMain/resources/Windows-x84/mupdf-lib.properties")
            }
            tasks.register("desktopResourceWindowsX86") {
                group = "build"
                description = "Windows x86 resources for desktop."
                dependsOn(
                    "buildJavaviewerlibWindowsX86",
                    "copyJavaviewerlibDll",
                    "generateMupdfLibPropertiesWindowsX86"
                )
            }

            // Windows amd64
            tasks.register<MsBuildTask>("buildJavaviewerlibWindowsAMD64") {
                group = "build"
                description = "Builds the native javaviewerlib using MSBuild."
                workingDirectory.set(layout.projectDirectory.dir("../mupdf/platform/win32/"))
                projectName.set("javaviewerlib.vcxproj")
                configuration.set("Release")
                platform.set("x64")
            }
            tasks.register<Copy>("copyJavaviewerlib64Dll") {
                mustRunAfter("buildJavaviewerlibWindowsAMD64")
                group = "build"
                description = "Copies javaviewerlib64.dll to the resources folder."
                from(file("../mupdf/platform/win32/x64/Release/javaviewerlib64.dll"))
                into(layout.projectDirectory.dir("src/jvmMain/resources/Windows-amd64"))
                rename { "mupdf_java.dll" }
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
            tasks.register<GenerateMupdfLibPropertiesTask>("generateMupdfLibPropertiesWindowsAMD64") {
                mustRunAfter("copyJavaviewerlib64Dll")
                libVersion = version.toString()
                dllFile =
                    layout.projectDirectory.file("src/jvmMain/resources/Windows-amd64/mupdf_java.dll")
                propsFile =
                    layout.projectDirectory.file("src/jvmMain/resources/Windows-amd64/mupdf-lib.properties")
            }
            tasks.register("desktopResourceWindowsAMD64") {
                group = "build"
                description = "Windows amd64 resources for desktop."
                dependsOn(
                    "buildJavaviewerlibWindowsAMD64",
                    "copyJavaviewerlib64Dll",
                    "generateMupdfLibPropertiesWindowsAMD64"
                )
            }

            tasks.register<AddUft8BomToFilesTask>("addUtf8BomToFiles") {
                group = "other"
                description = "Overwrite and save multiple UTF-8 BOM-free files with UTF-8 BOM."
                inputFiles.setFrom(
                    file("../mupdf/thirdparty/zxing-cpp/core/src/BitMatrixIO.cpp"),
                    file("../mupdf/thirdparty/zxing-cpp/core/src/WriteBarcode.cpp")
                )
            }

            // Linux amd64
            tasks.register<Exec>("buildLibmupdfLinuxAMD64") {
                group = "build"
                description = "Builds libmupdf_java64.so"
                commandLine("make", "java",)
            }
            tasks.register<Copy>("copyLibmupdfJava64So") {
                mustRunAfter("buildLibmupdfLinuxAMD64")
                group = "build"
                description = "Copies libmupdf_java64.so to the resources folder."
                from(file("../mupdf/build/java/release/libmupdf_java64.so"))
                into(layout.projectDirectory.dir("src/jvmMain/resources/Linux-amd64"))
                rename { "mupdf_java.so" }
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
            tasks.register<GenerateMupdfLibPropertiesTask>("generateMupdfLibPropertiesLinuxAMD64") {
                mustRunAfter("copyLibmupdfJava64So")
                libVersion = version.toString()
                dllFile =
                    layout.projectDirectory.file("src/jvmMain/resources/Linux-amd64/mupdf_java.so")
                propsFile =
                    layout.projectDirectory.file("src/jvmMain/resources/Linux-amd64/mupdf-lib.properties")
            }
            tasks.register("desktopResourceLinuxAMD64") {
                group = "build"
                description = "Windows amd64 resources for desktop."
                dependsOn(
                    "buildLibmupdfLinuxAMD64",
                    "copyLibmupdfJava64So",
                    "generateMupdfLibPropertiesLinuxAMD64"
                )
            }
        }
    }
}
