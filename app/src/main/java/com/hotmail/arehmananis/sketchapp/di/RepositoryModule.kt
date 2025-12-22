package com.hotmail.arehmananis.sketchapp.di

import com.hotmail.arehmananis.sketchapp.data.repository.PreferencesRepositoryImpl
import com.hotmail.arehmananis.sketchapp.data.repository.UserRepositoryImpl
import com.hotmail.arehmananis.sketchapp.domain.repository.PreferencesRepository
import com.hotmail.arehmananis.sketchapp.domain.repository.UserRepository
import com.hotmail.arehmananis.sketchapp.domain.usecase.GetCurrentUserUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.GetUserPreferencesUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.UpdateUserPreferencesUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.UpdateUserUseCase
import com.hotmail.arehmananis.sketchapp.presentation.feature.home.HomeViewModel
import com.hotmail.arehmananis.sketchapp.presentation.feature.profile.ProfileViewModel
import com.hotmail.arehmananis.sketchapp.presentation.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for repositories and use cases
 * Binds repository implementations to their interfaces
 */
val repositoryModule = module {

    /**
     * Repository bindings
     */
    single<UserRepository> { UserRepositoryImpl() }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }

    /**
     * Use case bindings
     */
    factory { GetCurrentUserUseCase(get()) }
    factory { UpdateUserUseCase(get()) }
    factory { GetUserPreferencesUseCase(get()) }
    factory { UpdateUserPreferencesUseCase(get()) }

    /**
     * ViewModel bindings
     */
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SettingsViewModel)
}
