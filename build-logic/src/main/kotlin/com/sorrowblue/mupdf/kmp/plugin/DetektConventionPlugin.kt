package com.sorrowblue.mupdf.kmp.plugin

import com.sorrowblue.mupdf.kmp.id
import com.sorrowblue.mupdf.kmp.libs
import com.sorrowblue.mupdf.kmp.plugins
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin

@Suppress("unused")
internal class DetektConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            plugins {
                id(libs.plugins.detekt)
            }

            dependencies {
                detektPlugins(libs.detekt.compose)
                detektPlugins(libs.detekt.formatting)
            }

            configure<DetektExtension> {
                buildUponDefaultConfig = true
                autoCorrect = true
                basePath = rootProject.projectDir.absolutePath
                config.setFrom("${rootProject.projectDir}/config/detekt/detekt.yml")
            }

            val reportMerge = rootProject.tasks.withType(ReportMergeTask::class)
            tasks.withType<Detekt>().configureEach {
                reports {
                    sarif.required.set(true)
                    html.required.set(false)
                    md.required.set(false)
                    txt.required.set(false)
                    xml.required.set(false)
                }
                finalizedBy(reportMerge)
                exclude {
                    it.file.path.run { contains("generated") || contains("buildkonfig") || contains("mupdf\\platform") || contains("mupdf/platform") }.also { b ->
                        if (b) {
                            logger.lifecycle("exclude path ${it.file.path}")
                        }
                    }
                }
            }
            reportMerge.configureEach {
                input.from(tasks.withType<Detekt>().map(Detekt::sarifReportFile))
            }

            mapOf(
                "detektAndroidAll" to "(?i)^(?!.*metadata).*android.*$".toRegex(),
                "detektDesktopAll" to "(?i)^(?!.*metadata).*desktop.*$".toRegex(),
                "detektMetadataAll" to "(?i)^.*metadata.*$".toRegex()
            ).forEach { (taskName, regex) ->
                tasks.register(taskName) {
                    group = LifecycleBasePlugin.VERIFICATION_GROUP
                    dependsOn(
                        tasks.withType<Detekt>()
                            .matching { detekt -> detekt.name.contains(regex) }
                    )
                }
            }
        }
    }

    private val Project.detektPlugins get() = configurations.getByName("detektPlugins")
}
