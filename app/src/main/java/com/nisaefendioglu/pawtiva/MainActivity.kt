package com.nisaefendioglu.pawtiva

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.feature.onboarding.OnboardingRoute
import com.nisaefendioglu.pawtiva.navigation.PawtivaApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity. Extends AppCompatActivity so AppCompatDelegate.setApplicationLocales
 * can switch the language instantly on every supported Android version.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val preferences = (state as? MainUiState.Ready)?.preferences
            val themeMode = preferences?.themeMode ?: ThemeMode.SYSTEM
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Keep status/navigation bar icons readable when the in-app theme differs from the system one.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose {}
            }

            PawtivaTheme(themeMode = themeMode, dynamicColor = preferences?.dynamicColor ?: false) {
                when {
                    // Window background matches the theme, so this is a seamless blank frame.
                    preferences == null -> Box(Modifier.fillMaxSize().background(PawtivaTheme.colors.background))
                    !preferences.onboardingCompleted -> OnboardingRoute()
                    else -> PawtivaApp()
                }
            }
        }
    }
}
