package com.wave.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wave.app.data.ThemeMode
import com.wave.app.ui.LauncherScreen
import com.wave.app.ui.LauncherViewModel
import com.wave.app.ui.theme.WavyLauncherTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val isDark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }
            val dynamic = settings.themeMode == ThemeMode.SYSTEM

            WavyLauncherTheme(
                darkTheme = isDark,
                dynamicColor = dynamic
            ) {
                LauncherScreen(viewModel = viewModel)
            }
        }
    }
}
