package org.sorrowblue.mupdf.kmp.document

import androidx.compose.runtime.staticCompositionLocalOf

expect abstract class PlatformContext

val LocalPlatformContext = staticCompositionLocalOf<PlatformContext> {
    error("No PlatformContext provided")
}
