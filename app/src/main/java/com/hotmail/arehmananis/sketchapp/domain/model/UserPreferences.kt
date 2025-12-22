package com.hotmail.arehmananis.sketchapp.domain.model

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true
)
