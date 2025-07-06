package com.sorrowblue.mupdfkmp.plugin

import java.security.MessageDigest
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

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
