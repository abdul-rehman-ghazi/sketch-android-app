package com.hotmail.arehmananis.sketchapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotmail.arehmananis.sketchapp.domain.model.ThemeMode
import com.hotmail.arehmananis.sketchapp.presentation.AuthViewModel
import com.hotmail.arehmananis.sketchapp.presentation.common.AppNavigation
import com.hotmail.arehmananis.sketchapp.presentation.feature.settings.SettingsViewModel
import com.hotmail.arehmananis.sketchapp.presentation.theme.SketchAppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModel()
    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme = shouldUseDarkTheme(settingsViewModel)
            val authUser by authViewModel.currentUser.collectAsStateWithLifecycle()

            SketchAppTheme(darkTheme = isDarkTheme) {
                AppNavigation(authUser = authUser)
            }
        }
    }
}

@Composable
private fun shouldUseDarkTheme(settingsViewModel: SettingsViewModel): Boolean {
    val uiState by settingsViewModel.uiState.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()

    return when (uiState) {
        is com.hotmail.arehmananis.sketchapp.presentation.feature.settings.SettingsUiState.Success -> {
            val preferences = (uiState as com.hotmail.arehmananis.sketchapp.presentation.feature.settings.SettingsUiState.Success).preferences
            when (preferences.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDarkTheme
            }
        }
        else -> systemInDarkTheme
    }
}
