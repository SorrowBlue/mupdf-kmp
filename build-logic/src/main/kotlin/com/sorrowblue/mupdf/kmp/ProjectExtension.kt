package com.sorrowblue.mupdf.kmp

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.use.PluginDependency

internal val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

internal fun Project.plugins(block: PluginManager.() -> Unit) = with(pluginManager, block)

internal fun PluginManager.id(provider: Provider<PluginDependency>) = apply(provider.get().pluginId)
