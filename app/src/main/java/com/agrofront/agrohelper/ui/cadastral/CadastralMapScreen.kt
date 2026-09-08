package com.agrofront.agrohelper.ui.cadastral

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastralMapScreen(onBack: () -> Unit) {
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val webView = webViewState.value
        if (webView?.canGoBack() == true) webView.goBack() else onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кадастровая карта") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(Color.TRANSPARENT)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    loadUrl("https://map.ru/pkk/")
                    webViewState.value = this
                }
            },
            update = { webViewState.value = it }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.apply {
                stopLoading()
                destroy()
            }
            webViewState.value = null
        }
    }
}
