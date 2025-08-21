package org.sorrowblue.mupdf.kmp.document

import com.artifex.mupdf.fitz.ColorSpace
import com.artifex.mupdf.fitz.DrawDevice
import com.artifex.mupdf.fitz.Matrix
import com.artifex.mupdf.fitz.Page
import com.artifex.mupdf.fitz.Pixmap
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.resolve

actual class PageWrapper(private val page: Page) {

    actual fun save(platformFile: PlatformFile, index: Int) {
        val bounds = page.bounds
        val pixmap = Pixmap(ColorSpace.DeviceRGB, bounds, false)
        pixmap.clear(0xff)
        val drawDevice = DrawDevice(pixmap)
        val displayList = page.toDisplayList()
        displayList.run(drawDevice, Matrix.Identity(), bounds, null)
        pixmap.saveAsJPEG(platformFile.resolve("page_$index.jpg").absolutePath(), 75)
    }
}
