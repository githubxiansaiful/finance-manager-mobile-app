package com.financemanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financemanager.app.data.local.AppThemeMode
import com.financemanager.app.ui.navigation.MainAppScaffold
import com.financemanager.app.ui.theme.FinanceTheme
import com.financemanager.app.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FinanceViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
            }

            FinanceTheme(darkTheme = isDark) {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}
