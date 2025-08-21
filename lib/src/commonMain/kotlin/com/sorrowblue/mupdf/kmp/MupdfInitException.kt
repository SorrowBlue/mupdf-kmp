package com.sorrowblue.mupdf.kmp

/**
 * Exception thrown when MuPDF initialization fails.
 *
 * @param message Detailed message describing the failure.
 * @param cause Cause of the failure.
 * @constructor Creates an instance of MuPDF initialization exception.
 */
class MupdfInitException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
