package com.sorrowblue.mupdf.kmp.primitive

import com.sorrowblue.mupdf.kmp.libs
import dev.detekt.gradle.Detekt

plugins {
    dev.detekt
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("${rootProject.projectDir}/config/detekt/detekt.yml")
    basePath = rootProject.projectDir
    autoCorrect = true
    parallel = true
}

dependencies {
    detektPlugins(libs.detekt.compose)
    detektPlugins(libs.detekt.ktlintWrapper)
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
            contains("generated") || contains("buildkonfig") ||
                contains("mupdf\\platform") || contains("mupdf/platform")
        }
    }
}
