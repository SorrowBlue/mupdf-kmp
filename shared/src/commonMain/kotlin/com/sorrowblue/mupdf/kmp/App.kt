package com.sorrowblue.mupdf.kmp

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sorrowblue.mupdf.kmp.icons.FilePdf
import kotlinx.coroutines.launch

@Composable
fun App(modifier: Modifier = Modifier) {
    MaterialTheme {
        val state = rememberAppState()
        App(
            uiState = state.uiState,
            onClickOpenPdf = state::onClickOpenPdf,
            onClickOpenDirectory = state::openDirectory,
            modifier = modifier,
        )
    }
}

data class AppUiState(val running: Boolean = false, val log: String = "", val output: String = "")

@Composable
private fun App(
    uiState: AppUiState,
    onClickOpenPdf: () -> Unit,
    onClickOpenDirectory: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.size(8.dp))

            Button(onClick = onClickOpenPdf) {
                Icon(FilePdf, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("PDFを選択する")
            }
            if (uiState.output.isNotEmpty()) {
                Text(
                    text = uiState.output,
                    style = TextStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline,
                    ),
                    modifier = Modifier.clickable {
                        onClickOpenDirectory()
                    },
                )
            }
            val scrollState = rememberScrollState()
            Text(
                text = uiState.log,
                modifier = Modifier.verticalScroll(scrollState),
            )
            val coroutineScope = rememberCoroutineScope()
            LaunchedEffect(uiState.log) {
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }
        }
        if (uiState.running) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(false) {},
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview
@Composable
private fun AppPreview() {
    MaterialTheme {
        App(
            uiState = AppUiState(),
            onClickOpenPdf = {},
            onClickOpenDirectory = {},
        )
    }
}
