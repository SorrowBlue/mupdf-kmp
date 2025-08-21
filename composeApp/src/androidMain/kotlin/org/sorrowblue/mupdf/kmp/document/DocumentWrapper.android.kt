package org.sorrowblue.mupdf.kmp.document

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.SeekableInputStream
import com.sorrowblue.mupdf.kmp.MuPDF
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.uri

@SuppressLint("StaticFieldLeak")
actual object DocumentWrapper {

    private lateinit var stream: DeviceSeekableInputStream
    private lateinit var document: Document

    init {
        MuPDF.init()
    }

    actual fun openDocument(context: PlatformContext, platformFile: PlatformFile) {
        stream = DeviceSeekableInputStream(context, platformFile.uri)
        document = Document.openDocument(stream, "pdf")
    }

    actual fun loadPage(index: Int): PageWrapper {
        return PageWrapper(document.loadPage(index))
    }

    actual fun countPage(): Int {
        return document.countPages()
    }
}

private class DeviceSeekableInputStream(context: Context, uri: Uri) : SeekableInputStream, AutoCloseable {

    private val input = ParcelFileDescriptor.AutoCloseInputStream(
        context.contentResolver.openFileDescriptor(uri, "r")
    )

    override fun seek(offset: Long, whence: Int): Long {
        when (whence) {
            SeekableInputStream.SEEK_SET -> input.channel.position(offset)
            SeekableInputStream.SEEK_CUR -> input.channel.position(input.channel.position() + offset)
            SeekableInputStream.SEEK_END -> input.channel.position(input.channel.size() + offset)
        }
        return input.channel.position()
    }

    override fun position(): Long {
        return input.channel.position()
    }

    override fun read(buf: ByteArray): Int {
        return input.read(buf)
    }

    override fun close() {
        input.close()
    }
}
