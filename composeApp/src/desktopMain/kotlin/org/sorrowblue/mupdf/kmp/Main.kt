package org.sorrowblue.mupdf.kmp

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sorrowblue.mupdf.kmp.MuPDF

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "mupdf-kmp") {
        App()
    }
    LaunchedEffect(Unit) {
        MuPDF.init()
    }
}
