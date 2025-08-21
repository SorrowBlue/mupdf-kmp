package org.sorrowblue.mupdf.kmp.document

import android.graphics.Bitmap
import android.provider.DocumentsContract
import com.artifex.mupdf.fitz.Page
import com.artifex.mupdf.fitz.android.AndroidDrawDevice
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.uri

actual class PageWrapper(private val page: Page) {

    actual fun save(platformContext: PlatformContext, platformFile: PlatformFile, index: Int) {
        val directoryUri = platformFile.uri
        val docTreeUri = DocumentsContract.buildDocumentUriUsingTree(
            directoryUri,
            DocumentsContract.getTreeDocumentId(directoryUri)
        )
        val newFileUri = DocumentsContract.createDocument(
            platformContext.contentResolver,
            docTreeUri,
            "image/jpeg",
            "page_$index.jpg"
        )
        newFileUri?.let {
            platformContext.contentResolver.openOutputStream(it)?.use { outputStream ->
                AndroidDrawDevice.drawPage(page, 300f)
                    .compress(Bitmap.CompressFormat.WEBP_LOSSY, 50, outputStream)
            }
        }
    }
}
