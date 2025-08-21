package org.sorrowblue.mupdf.kmp.document

import io.github.vinceglb.filekit.PlatformFile

expect object DocumentWrapper {
    fun openDocument(platformFile: PlatformFile)
    fun loadPage(index: Int): PageWrapper
    fun countPage(): Int
}
