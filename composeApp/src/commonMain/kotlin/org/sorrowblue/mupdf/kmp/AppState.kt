package org.sorrowblue.mupdf.kmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.PickerResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sorrowblue.mupdf.kmp.document.DocumentWrapper
import org.sorrowblue.mupdf.kmp.document.LocalPlatformContext
import org.sorrowblue.mupdf.kmp.document.PlatformContext
import org.sorrowblue.mupdf.kmp.document.PlatformDirectory
import org.sorrowblue.mupdf.kmp.document.rememberPlatformDirectory

interface AppState {

    val uiState: AppUiState

    fun onClickOpenPdf()
    fun openDirectory()
}

@Composable
fun rememberAppState(): AppState {
    val platformDirectory = rememberPlatformDirectory()
    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current
    val state = remember { AppStateImpl(context = context, scope = scope) }
    state.platformDirectory = platformDirectory
    state.filePickerLauncher = rememberFilePickerLauncher(FileKitType.File("pdf")) { file ->
        state.onResultFile(file)
    }
    state.directoryPickerLauncher =
        rememberDirectoryPickerLauncher("PDFのページを展開するフォルダを選択") { file ->
            state.onResultDirectory(file)
        }
    return state
}

private class AppStateImpl(
    private val context: PlatformContext,
    private val scope: CoroutineScope,
) : AppState {

    lateinit var platformDirectory: PlatformDirectory
    lateinit var filePickerLauncher: PickerResultLauncher
    lateinit var directoryPickerLauncher: PickerResultLauncher

    override var uiState by mutableStateOf(AppUiState())

    override fun onClickOpenPdf() {
        filePickerLauncher.launch()
        uiState = uiState.copy(
            running = true,
            log = "",
            output = ""
        )
    }

    override fun openDirectory() {
        platformDirectory.openDirectory(uiState.output, ::log)
    }

    fun onResultFile(file: PlatformFile?) {
        file ?: return run {
            uiState = uiState.copy(running = false)
        }
        println("onResultFile")
        uiState = uiState.copy(running = true)
        scope.launch {
            withContext(Dispatchers.IO) {
                DocumentWrapper.openDocument(context, file)
                val pageCount = DocumentWrapper.countPage()
                log("PDFをひらいています。ファイル名: ${file.name}、ページ数: $pageCount")
            }
            delay(1000)
            directoryPickerLauncher.launch()
        }
    }

    fun onResultDirectory(file: PlatformFile?) {
        file ?: return run {
            uiState = uiState.copy(running = false)
        }
        println("onResultDirectory")
        uiState = uiState.copy(running = true)
        scope.launch {
            delay(2000)
            withContext(Dispatchers.IO) {
                val pageCount = DocumentWrapper.countPage()
                for (i in 0 until pageCount) {
                    log("${i + 1}/$pageCount ページを保存しています")
                    val page = DocumentWrapper.loadPage(i)
                    page.save(context, file, i)
                }
                log("全${pageCount}ページを保存しました。")
            }
            uiState = uiState.copy(output = file.path, running = false)
        }
    }

    private fun log(text: String) {
        uiState = uiState.copy(
            log = """
                ${uiState.log}
                $text
            """.trimIndent()
        )
    }
}
