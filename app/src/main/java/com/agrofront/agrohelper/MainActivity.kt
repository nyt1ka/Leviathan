package com.agrofront.agrohelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.agrofront.agrohelper.ui.AgroHelperApp
import com.agrofront.agrohelper.ui.splash.SplashScreen
import com.agrofront.agrohelper.ui.theme.AgroHelperTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroHelperTheme {
                var showSplash by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(1400)
                    showSplash = false
                }
                if (showSplash) SplashScreen() else AgroHelperApp()
            }
        }
    }
}
