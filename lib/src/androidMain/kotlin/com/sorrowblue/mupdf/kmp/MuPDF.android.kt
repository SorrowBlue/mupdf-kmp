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
            } catch (e: UnsatisfiedLinkError) {
                try {
                    System.loadLibrary("mupdf_java64")
                } catch (ee: UnsatisfiedLinkError) {
                    System.loadLibrary("mupdf_java32")
                }
            }
            if (Context.initNative() < 0) throw RuntimeException("cannot initialize mupdf library")
        }
    }
}
