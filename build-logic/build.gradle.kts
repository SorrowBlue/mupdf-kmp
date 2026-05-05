import dev.detekt.gradle.Detekt
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
    detektPlugins(libs.detekt.ktlintWrapper)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(layout.projectDirectory.file("../config/detekt/detekt.yml"))
    basePath.set(projectDir)
    autoCorrect = true
    parallel = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(false)
        markdown.required.set(false)
        sarif.required.set(true)
        checkstyle.required.set(false)
    }
    exclude {
        it.file.path.run { contains("generated-sources") }
    }
}

gradlePlugin {
    plugins {
        register(libs.plugins.mupdfKmp.muBuild) {
            implementationClass = "com.sorrowblue.mupdf.kmp.plugin.MsBuildPlugin"
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
