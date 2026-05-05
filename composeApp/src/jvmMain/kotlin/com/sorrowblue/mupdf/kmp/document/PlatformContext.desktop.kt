package com.sorrowblue.mupdf.kmp.document

@Suppress("AbstractClassCanBeInterface")
actual abstract class PlatformContext private constructor() {

    companion object : PlatformContext()
}
