package org.sorrowblue.mupdf.kmp.document

import io.github.vinceglb.filekit.PlatformFile

expect class PageWrapper {

    fun save(platformFile: PlatformFile, index: Int)
}
