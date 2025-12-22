package com.hotmail.arehmananis.sketchapp.di

import com.hotmail.arehmananis.sketchapp.data.local.PreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for app-level dependencies
 * Provides DataStore and other Android-specific dependencies
 */
val appModule = module {

    /**
     * Provides PreferencesDataStore with Android Context
     */
    single {
        PreferencesDataStore(androidContext())
    }
}
