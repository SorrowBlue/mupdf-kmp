import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.kotlin.dsl.withType

plugins {
    `kotlin-dsl`
    alias(libs.plugins.detekt)
}

group = "com.sorrowblue.mupdf.kmp.buildlogic"

kotlin {
    jvmToolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

dependencies {
    compileOnly(files(currentLibs.javaClass.superclass.protectionDomain.codeSource.location))
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    detektPlugins(libs.detekt.compose)
    detektPlugins(libs.detekt.formatting)
}

detekt {
    buildUponDefaultConfig = true
    autoCorrect = true
    config.setFrom(layout.projectDirectory.file("../config/detekt/detekt.yml"))
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(false)
        md.required.set(false)
        sarif.required.set(true)
        txt.required.set(false)
        xml.required.set(false)
    }
}

tasks.register("detektAll") {
    group = "verification"
    dependsOn(tasks.withType<Detekt>())
}

gradlePlugin {
    plugins {
        register(libs.plugins.mupdfKmp.muBuild) {
            implementationClass = "com.sorrowblue.mupdf.kmp.plugin.MsBuildPlugin"
        }
        register(libs.plugins.mupdfKmp.gitTagVersion) {
            implementationClass = "com.sorrowblue.mupdf.kmp.plugin.GitTagVersionPlugin"
        }
        register(libs.plugins.mupdfKmp.detekt) {
            implementationClass = "com.sorrowblue.mupdf.kmp.plugin.DetektConventionPlugin"
        }
        register(libs.plugins.mupdfKmp.lint) {
            implementationClass = "com.sorrowblue.mupdf.kmp.plugin.AndroidLintConventionPlugin"
        }
    }
}

private val currentLibs get() = libs

private fun NamedDomainObjectContainer<PluginDeclaration>.register(
    provider: Provider<PluginDependency>,
    function: PluginDeclaration.() -> Unit,
) = register(provider.get().pluginId) {
    id = name
    function()
}
