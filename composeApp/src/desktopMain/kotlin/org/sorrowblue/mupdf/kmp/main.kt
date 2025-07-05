package org.sorrowblue.mupdf.kmp

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.artifex.mupdf.fitz.ColorSpace
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.DrawDevice
import com.artifex.mupdf.fitz.Matrix
import com.artifex.mupdf.fitz.MuPDF
import com.artifex.mupdf.fitz.Pixmap

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "mupdf-kmp",
    ) {
        App()
    }
    LaunchedEffect(Unit) {
        MuPDF
        val doc = Document.openDocument("C:\\Users\\sorro\\Documents\\iijmio.jp_imh_signup_#_Complete.pdf")
        println("Document opened: ${doc.countPages()} pages")
        for (i in 0 until doc.countPages()) {
            val page = doc.loadPage(i)
            val bounds = page.getBounds()
            val pixmap = Pixmap(ColorSpace.DeviceRGB, bounds, false)
            pixmap.clear(0xff)
            val drawDevice = DrawDevice(pixmap)
            val displayList = page.toDisplayList()
            displayList.run(drawDevice, Matrix.Identity(), bounds, null)
            pixmap.saveAsJPEG("C:\\Users\\sorro\\Documents\\page_$i.png", 75)
        }
    }
}
