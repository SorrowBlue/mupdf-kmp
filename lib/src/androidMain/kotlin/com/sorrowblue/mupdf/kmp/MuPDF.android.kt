package com.sorrowblue.mupdf.kmp

import com.artifex.mupdf.fitz.Context

actual object MuPDF {
    var inited = false
        private set
    actual fun init() {
        if (!inited) {
            inited = true
            try {
                System.loadLibrary("mupdf_java")
            } catch (_: UnsatisfiedLinkError) {
                try {
                    System.loadLibrary("mupdf_java64")
                } catch (_: UnsatisfiedLinkError) {
                    System.loadLibrary("mupdf_java32")
                }
            }
            if (Context.initNative() < 0) throw MupdfInitException("cannot initialize mupdf library")
        }
    }
}
