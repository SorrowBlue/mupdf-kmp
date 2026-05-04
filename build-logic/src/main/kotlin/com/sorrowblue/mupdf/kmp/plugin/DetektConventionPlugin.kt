package com.sorrowblue.mupdf.kmp.plugin

import com.sorrowblue.mupdf.kmp.id
import com.sorrowblue.mupdf.kmp.libs
import com.sorrowblue.mupdf.kmp.plugins
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import kotlin.text.set
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

@Suppress("unused")
internal class DetektConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            plugins {
                id(libs.plugins.detekt)
            }

            dependencies {
                detektPlugins(libs.detekt.compose)
                detektPlugins(libs.detekt.ktlintWrapper)
            }

            configure<DetektExtension> {
                buildUponDefaultConfig.set(true)
                config.setFrom("${rootProject.projectDir}/config/detekt/detekt.yml")
                basePath.set(rootProject.projectDir)
                autoCorrect.set(true)
                parallel.set(true)
            }

            tasks.withType<Detekt>().configureEach {
                reports {
                    sarif.required.set(true)
                    html.required.set(false)
                    markdown.required.set(false)
                    checkstyle.required.set(false)
                }
                exclude {
                    it.file.path.run {
                        contains("generated") || contains("buildkonfig") || contains("mupdf\\platform") || contains("mupdf/platform")
                    }.also { exclude ->
                        if (exclude) {
                            logger.lifecycle("exclude path ${it.file.path}")
                        }
                    }
                }
            }
        }
    }

    private val Project.detektPlugins get() = configurations.getByName("detektPlugins")
}
