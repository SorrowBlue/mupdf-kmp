package org.sorrowblue.mupdf.kmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sorrowblue.mupdf.kmp.icons.FilePdf

@Composable
fun App(modifier: Modifier = Modifier) {
    MaterialTheme {
        val state = rememberAppState()
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "PDFのページを画像ファイルとして保存します",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.size(8.dp))

                Button(onClick = state::onClickOpenPdf) {
                    Icon(FilePdf, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("PDFを選択する")
                }
                if (state.uiState.output.isNotEmpty()) {
                    Text(
                        text = state.uiState.output,
                        style = TextStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable {
                            state.openDirectory()
                        }
                    )
                }
                val scrollState = rememberScrollState()
                Text(
                    text = state.uiState.log,
                    modifier = Modifier.verticalScroll(scrollState)
                )
                val coroutineScope = rememberCoroutineScope()
                LaunchedEffect(state.uiState.log) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
            }
            if (state.uiState.running) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                        .clickable(false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

data class AppUiState(
    val running: Boolean = false,
    val log: String = "",
    val output: String = "",
)
