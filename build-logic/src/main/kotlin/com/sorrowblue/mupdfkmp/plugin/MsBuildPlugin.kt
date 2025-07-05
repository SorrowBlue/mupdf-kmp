package com.sorrowblue.mupdfkmp.plugin

import java.security.MessageDigest
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register

class MsBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {

        target.tasks.register<MsBuildTask>("buildJavaviewerlibx86") {
            group = "build"
            description = "Builds the native javaviewerlib using MSBuild."
            workingDirectory.set(target.layout.projectDirectory.dir("../mupdf/platform/win32/"))
            projectName.set("javaviewerlib.vcxproj")
            configuration.set("Release")
            platform.set("x86")
            finalizedBy("copyJavaviewerlibDll")
        }
        target.tasks.register<MsBuildTask>("buildJavaviewerlibx64") {
            group = "build"
            description = "Builds the native javaviewerlib using MSBuild."
            workingDirectory.set(target.layout.projectDirectory.dir("../mupdf/platform/win32/"))
            projectName.set("javaviewerlib.vcxproj")
            configuration.set("Release")
            platform.set("x64")
            finalizedBy("copyJavaviewerlib64Dll")
        }

        target.tasks.register<Copy>("copyJavaviewerlib64Dll") {
            dependsOn("buildJavaviewerlibx64")
            group = "build"
            description = "Copies javaviewerlib64.dll to the resources folder."
            from(target.file("../mupdf/platform/win32/x64/Release/javaviewerlib64.dll"))
            into(target.layout.projectDirectory.dir("src/jvmMain/resources/Windows-amd64"))
            rename { "mupdf_java.dll" }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            finalizedBy("generateMupdfLibProperties-amd64")
        }
        target.tasks.register<Copy>("copyJavaviewerlibDll") {
            dependsOn("buildJavaviewerlibx86")
            group = "build"
            description = "Copies javaviewerlib64.dll to the resources folder."
            from(target.file("../mupdf/platform/win32/Release/javaviewerlib.dll"))
            into(target.layout.projectDirectory.dir("src/jvmMain/resources/Windows-x84"))
            rename { "mupdf_java.dll" }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            finalizedBy("generateMupdfLibProperties-x84")
        }


        target.tasks.register<GenerateMupdfLibPropertiesTask>("generateMupdfLibProperties-amd64") {
            this.libVersion = target.version.toString()
            this.dllFile =
                target.layout.projectDirectory.file("src/jvmMain/resources/Windows-amd64/mupdf_java.dll")
            this.propsFile =
                target.layout.projectDirectory.file("src/jvmMain/resources/Windows-amd64/mupdf-lib.properties")
        }
        target.tasks.register<GenerateMupdfLibPropertiesTask>("generateMupdfLibProperties-x84") {
            this.libVersion = target.version.toString()
            this.dllFile =
                target.layout.projectDirectory.file("src/jvmMain/resources/Windows-x84/mupdf_java.dll")
            this.propsFile =
                target.layout.projectDirectory.file("src/jvmMain/resources/Windows-x84/mupdf-lib.properties")
        }

        target.tasks.register<AddUft8BomToFilesTask>("addUtf8BomToFiles") {
            group = "build"
            description = "複数のUTF-8 BOMなしファイルをUTF-8 BOM付きで上書き保存する。"
            inputFiles.setFrom(
                target.file("../mupdf/thirdparty/zxing-cpp/core/src/BitMatrixIO.cpp"),
                target.file("../mupdf/thirdparty/zxing-cpp/core/src/WriteBarcode.cpp")
            )
        }
    }
}

abstract class MsBuildTask : Exec() {

    @get:InputDirectory
    abstract val workingDirectory: DirectoryProperty

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val configuration: Property<String>

    @get:Input
    abstract val platform: Property<String>

    @TaskAction
    fun task() {
        workingDir = workingDirectory.get().asFile
        commandLine(
            "MsBuild.exe",
            projectName.get(),
            "/p:configuration=${configuration.get()}",
            "/p:platform=${platform.get()}"
        )
    }
}

abstract class GenerateMupdfLibPropertiesTask : DefaultTask() {

    @get:InputFile
    abstract val dllFile: RegularFileProperty

    @get:Input
    abstract val libVersion: Property<String>

    @get:OutputFile
    abstract val propsFile: RegularFileProperty

    @TaskAction
    fun generateProperties() {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(dllFile.get().asFile.readBytes())
            .joinToString("") { "%02x".format(it) }
        propsFile.get().asFile.writeText(
            """
            build.ref=${libVersion.get()}
            lib.name=${dllFile.get().asFile.name}
            lib.hash=$hash
            """.trimIndent()
        )
    }
}
