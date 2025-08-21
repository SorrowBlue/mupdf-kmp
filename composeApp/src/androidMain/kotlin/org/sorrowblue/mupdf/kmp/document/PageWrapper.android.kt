package org.sorrowblue.mupdf.kmp.document

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.provider.DocumentsContract
import com.artifex.mupdf.fitz.Page
import com.artifex.mupdf.fitz.android.AndroidDrawDevice
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.uri

actual class PageWrapper(private val page: Page) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    actual fun save(platformFile: PlatformFile, index: Int) {
        val directoryUri = platformFile.uri
        val docTreeUri = DocumentsContract.buildDocumentUriUsingTree(
            directoryUri,
            DocumentsContract.getTreeDocumentId(directoryUri)
        )
        val newFileUri = DocumentsContract.createDocument(
            context.contentResolver,
            docTreeUri,
            "image/jpeg",
            "page_$index.jpg"
        )
        newFileUri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                AndroidDrawDevice.drawPage(page, 300f)
                    .compress(Bitmap.CompressFormat.WEBP_LOSSY, 50, outputStream)
            }
        }
    }
}
