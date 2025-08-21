package org.sorrowblue.mupdf.kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.sorrowblue.mupdf.kmp.document.DocumentWrapper
import org.sorrowblue.mupdf.kmp.document.PageWrapper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        DocumentWrapper.context = this
        PageWrapper.context = this
        setContent {
            App()
        }
    }
}
