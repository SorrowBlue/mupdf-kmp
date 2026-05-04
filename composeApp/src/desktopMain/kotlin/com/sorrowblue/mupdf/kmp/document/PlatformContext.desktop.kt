package com.sorrowblue.mupdf.kmp.document

import com.sorrowblue.mupdf.kmp.document.PlatformContext

@Suppress("UnnecessaryAbstractClass")
actual abstract class PlatformContext private constructor() {

    companion object : PlatformContext()
}
