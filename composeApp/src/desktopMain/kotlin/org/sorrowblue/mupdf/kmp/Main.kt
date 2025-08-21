package org.sorrowblue.mupdf.kmp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sorrowblue.mupdf.kmp.MuPDF
import org.sorrowblue.mupdf.kmp.document.LocalPlatformContext
import org.sorrowblue.mupdf.kmp.document.PlatformContext

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "mupdf-kmp") {
        CompositionLocalProvider(LocalPlatformContext provides PlatformContext) {
            App()
        }
    }
    LaunchedEffect(Unit) {
        MuPDF.init()
    }
}
