package org.sorrowblue.mupdf.kmp.document

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberPlatformDirectory(): PlatformDirectory {
    return remember { PlatformDirectory() }
}

actual class PlatformDirectory {
    actual fun openDirectory(path: String, error: (String) -> Unit) {
        if (Desktop.isDesktopSupported()) {
            try {
                // フォルダが存在し、かつディレクトリであることを確認
                val folder = File(path)
                if (folder.exists() && folder.isDirectory) {
                    Desktop.getDesktop().open(folder)
                } else {
                    error("エラー: フォルダが見つからないか、ディレクトリではありません: $path")
                }
            } catch (e: Exception) {
                error("エラー: フォルダを開けませんでした。")
                error(e.stackTraceToString())
            }
        } else {
            error("エラー: Desktop APIがサポートされていません。")
        }
    }
}
