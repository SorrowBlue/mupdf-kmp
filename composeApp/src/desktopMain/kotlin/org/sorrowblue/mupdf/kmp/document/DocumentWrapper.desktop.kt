package org.sorrowblue.mupdf.kmp.document

import com.artifex.mupdf.fitz.Document
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath

actual object DocumentWrapper {

    lateinit var document: Document

    actual fun openDocument(context: PlatformContext, platformFile: PlatformFile) {
        document = Document.openDocument(platformFile.absolutePath())
    }

    actual fun loadPage(index: Int): PageWrapper {
        return PageWrapper(document.loadPage(index))
    }

    actual fun countPage(): Int {
        return document.countPages()
    }
}
