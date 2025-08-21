package com.sorrowblue.mupdf.kmp.task

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class MsBuildTask : Exec() {

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
