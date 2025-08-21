package org.sorrowblue.mupdf.kmp.document

import io.github.vinceglb.filekit.PlatformFile

expect class PageWrapper {

    fun save(platformContext: PlatformContext, platformFile: PlatformFile, index: Int)
}
