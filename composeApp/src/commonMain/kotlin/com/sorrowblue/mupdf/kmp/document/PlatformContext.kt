package com.sorrowblue.mupdf.kmp.document

import androidx.compose.runtime.staticCompositionLocalOf

@Suppress("AbstractClassCanBeInterface")
expect abstract class PlatformContext

val LocalPlatformContext = staticCompositionLocalOf<PlatformContext> {
    error("No PlatformContext provided")
}
