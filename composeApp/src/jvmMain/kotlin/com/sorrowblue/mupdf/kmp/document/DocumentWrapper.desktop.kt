package com.sorrowblue.mupdf.kmp.document

import com.artifex.mupdf.fitz.Document
import com.sorrowblue.mupdf.kmp.document.PageWrapper
import com.sorrowblue.mupdf.kmp.document.PlatformContext
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath

actual object DocumentWrapper {

    lateinit var document: Document

    actual fun openDocument(context: PlatformContext, platformFile: PlatformFile) {
        document = Document.openDocument(platformFile.absolutePath())
    }

    actual fun loadPage(index: Int): PageWrapper = PageWrapper(document.loadPage(index))

    actual fun countPage(): Int = document.countPages()
}
