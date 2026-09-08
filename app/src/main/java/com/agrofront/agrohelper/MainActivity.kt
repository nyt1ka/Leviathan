package com.agrofront.agrohelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.agrofront.agrohelper.ui.AgroHelperApp
import com.agrofront.agrohelper.ui.theme.AgroHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroHelperTheme {
                AgroHelperApp()
            }
        }
    }
}
