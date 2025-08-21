package org.sorrowblue.mupdf.kmp.document

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
actual fun rememberPlatformDirectory(): PlatformDirectory {
    val context = LocalContext.current
    return remember { PlatformDirectory(context = context) }
}

actual class PlatformDirectory(private val context: Context) {
    actual fun openDirectory(path: String, error: (String) -> Unit) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = path.toUri()
        }
        context.startActivity(intent)
    }
}
