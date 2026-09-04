package com.financemanager.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.financemanager.app.data.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("finance_user_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow(loadUser())
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val saved = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return runCatching { AppThemeMode.valueOf(saved) }.getOrDefault(AppThemeMode.SYSTEM)
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadUser(): UserAccount? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val photo = prefs.getString(KEY_USER_PHOTO, null)
        return UserAccount(id = id, email = email, displayName = name, photoUrl = photo)
    }

    fun saveSession(user: UserAccount) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_NAME, user.displayName)
            .putString(KEY_USER_PHOTO, user.photoUrl)
            .apply()
        _currentUser.value = user
    }

    fun clearSession() {
        // Keep theme preference intact across logout
        val currentTheme = _themeMode.value
        prefs.edit().clear().apply()
        setThemeMode(currentTheme)
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHOTO = "user_photo"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
