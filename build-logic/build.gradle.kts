plugins {
    `kotlin-dsl`
}

group = "com.sorrowblue.mupdfkmp.buildlogic"

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
}

gradlePlugin {
    plugins {
        register(libs.plugins.muBuild) {
            implementationClass = "com.sorrowblue.mupdfkmp.plugin.MsBuildPlugin"
        }
        register(libs.plugins.gitTagVersion) {
            implementationClass = "com.sorrowblue.mupdfkmp.plugin.GitTagVersionPlugin"
        }
    }
}

// Temporarily set to PushMode only
private val currentLibs get() = libs

private fun NamedDomainObjectContainer<PluginDeclaration>.register(
    provider: Provider<PluginDependency>,
    function: PluginDeclaration.() -> Unit,
) = register(provider.get().pluginId) {
    id = name
    function()
}
