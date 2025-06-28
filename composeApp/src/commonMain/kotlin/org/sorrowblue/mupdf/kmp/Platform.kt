package org.sorrowblue.mupdf.kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform