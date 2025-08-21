package org.sorrowblue.mupdf.kmp.document

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPlatformDirectory(): PlatformDirectory

expect class PlatformDirectory {

    fun openDirectory(path: String, error: (String) -> Unit)
}
