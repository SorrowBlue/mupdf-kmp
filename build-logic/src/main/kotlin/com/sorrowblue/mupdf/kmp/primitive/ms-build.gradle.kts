package com.sorrowblue.mupdf.kmp.primitive

import com.sorrowblue.mupdf.kmp.task.AddUft8BomToFilesTask
import com.sorrowblue.mupdf.kmp.task.GenerateMupdfLibPropertiesTask
import com.sorrowblue.mupdf.kmp.task.MsBuildTask

private val mupdfLibProperties = "mupdf-lib.properties"
private val mupdfJavaDll = "mupdf_java.dll"
private val mupdfJavaSo = "mupdf_java.so"
private val windowsX84 = "Windows-x84"
private val windowsAmd64 = "Windows-amd64"
private val linuxAmd64 = "Linux-amd64"

private val addUtf8BomToFiles = tasks.register<AddUft8BomToFilesTask>("addUtf8BomToFiles") {
    group = "other"
    description = "Overwrite and save multiple UTF-8 BOM-free files with UTF-8 BOM."
    inputFiles.setFrom(
        file("../mupdf/thirdparty/zxing-cpp/core/src/BitMatrixIO.cpp"),
        file("../mupdf/thirdparty/zxing-cpp/core/src/WriteBarcode.cpp"),
    )
}

// Windows x86
private val buildJavaviewerlibWindowsX86 =
    tasks.register<MsBuildTask>("buildJavaviewerlibWindowsX86") {
        dependsOn(addUtf8BomToFiles)
        group = "build"
        description = "Builds the native javaviewerlib using MSBuild."
        workingDirectory.set(layout.projectDirectory.dir("../mupdf/platform/win32/"))
        projectName.set("javaviewerlib.vcxproj")
        configuration.set("Release")
        platform.set("x86")
    }
private val copyJavaviewerlibDll = tasks.register<Copy>("copyJavaviewerlibDll") {
    mustRunAfter(buildJavaviewerlibWindowsX86)
    group = "build"
    description = "Copies javaviewerlib.dll to the resources folder."
    from(file("../mupdf/platform/win32/Release/javaviewerlib.dll"))
    into(layout.projectDirectory.dir("src/jvmMain/resources/$windowsX84"))
    rename { mupdfJavaDll }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
private val generateMupdfLibPropertiesWindowsX86 =
    tasks.register<GenerateMupdfLibPropertiesTask>("generateMupdfLibPropertiesWindowsX86") {
        mustRunAfter(copyJavaviewerlibDll)
        group = "build"
        libVersion.set(version.toString())
        dllFile.set(layout.projectDirectory.file("src/jvmMain/resources/$windowsX84/$mupdfJavaDll"))
        propsFile.set(
            layout.projectDirectory.file("src/jvmMain/resources/$windowsX84/$mupdfLibProperties")
        )
    }
tasks.register("desktopResourceWindowsX86") {
    group = "build"
    description = "Windows x86 resources for desktop."
    dependsOn(
        buildJavaviewerlibWindowsX86,
        copyJavaviewerlibDll,
        generateMupdfLibPropertiesWindowsX86,
    )
}

// Windows amd64
private val buildJavaviewerlibWindowsAMD64 =
    tasks.register<MsBuildTask>("buildJavaviewerlibWindowsAMD64") {
        dependsOn(addUtf8BomToFiles)
        group = "build"
        description = "Builds the native javaviewerlib using MSBuild."
        workingDirectory.set(layout.projectDirectory.dir("../mupdf/platform/win32/"))
        projectName.set("javaviewerlib.vcxproj")
        configuration.set("Release")
        platform.set("x64")
    }
private val copyJavaviewerlib64Dll = tasks.register<Copy>("copyJavaviewerlib64Dll") {
    mustRunAfter(buildJavaviewerlibWindowsAMD64)
    group = "build"
    description = "Copies javaviewerlib64.dll to the resources folder."
    from(file("../mupdf/platform/win32/x64/Release/javaviewerlib64.dll"))
    into(layout.projectDirectory.dir("src/jvmMain/resources/$windowsAmd64"))
    rename { mupdfJavaDll }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
private val generateMupdfLibPropertiesWindowsAMD64 =
    tasks.register<GenerateMupdfLibPropertiesTask>("generateMupdfLibPropertiesWindowsAMD64") {
        mustRunAfter(copyJavaviewerlib64Dll)
        group = "build"
        libVersion.set(version.toString())
        dllFile.set(
            layout.projectDirectory.file("src/jvmMain/resources/$windowsAmd64/$mupdfJavaDll")
        )
        propsFile.set(
            layout.projectDirectory.file("src/jvmMain/resources/$windowsAmd64/$mupdfLibProperties")
        )
    }
tasks.register("desktopResourceWindowsAMD64") {
    group = "build"
    description = "Windows amd64 resources for desktop."
    dependsOn(
        buildJavaviewerlibWindowsAMD64,
        copyJavaviewerlib64Dll,
        generateMupdfLibPropertiesWindowsAMD64,
    )
}

// Linux amd64
private val buildLibmupdfLinuxAMD64 = tasks.register<Exec>("buildLibmupdfLinuxAMD64") {
    group = "build"
    description = "Builds libmupdf_java64.so"
    workingDir = file("../mupdf/")
    commandLine("make", "java")
}
private val copyLibmupdfJava64So = tasks.register<Copy>("copyLibmupdfJava64So") {
    mustRunAfter(buildLibmupdfLinuxAMD64)
    group = "build"
    description = "Copies libmupdf_java64.so to the resources folder."
    from(file("../mupdf/build/java/release/libmupdf_java64.so"))
    into(layout.projectDirectory.dir("src/jvmMain/resources/$linuxAmd64"))
    rename { mupdfJavaSo }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
private val generateMupdfLibPropertiesLinuxAMD64 =
    tasks.register<GenerateMupdfLibPropertiesTask>("generateMupdfLibPropertiesLinuxAMD64") {
        mustRunAfter(copyLibmupdfJava64So)
        group = "build"
        libVersion.set(version.toString())
        dllFile.set(layout.projectDirectory.file("src/jvmMain/resources/$linuxAmd64/$mupdfJavaSo"))
        propsFile.set(
            layout.projectDirectory.file("src/jvmMain/resources/$linuxAmd64/$mupdfLibProperties")
        )
    }
tasks.register("desktopResourceLinuxAMD64") {
    group = "build"
    description = "Windows amd64 resources for desktop."
    dependsOn(
        buildLibmupdfLinuxAMD64,
        copyLibmupdfJava64So,
        generateMupdfLibPropertiesLinuxAMD64,
    )
}
