package org.sorrowblue.mupdf.kmp.document

@Suppress("UnnecessaryAbstractClass")
actual abstract class PlatformContext private constructor() {

    companion object : PlatformContext()
}
