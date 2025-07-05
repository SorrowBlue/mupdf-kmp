package com.sorrowblue.mupdfkmp.plugin

import java.nio.file.Files
import java.nio.file.StandardOpenOption
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class AddUft8BomToFilesTask : DefaultTask() {

    @get:InputFiles
    abstract val inputFiles: ConfigurableFileCollection

    @Suppress("NewApi")
    @TaskAction
    fun addBomToFiles() {
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        inputFiles.forEach { inputFile ->
            val content = inputFile.readBytes()
            Files.write(
                inputFile.toPath(),
                bom + content,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }
}
