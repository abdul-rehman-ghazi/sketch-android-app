package com.hotmail.arehmananis.sketchapp.di

import com.hotmail.arehmananis.sketchapp.data.repository.*
import com.hotmail.arehmananis.sketchapp.domain.repository.*
import com.hotmail.arehmananis.sketchapp.domain.usecase.*
import com.hotmail.arehmananis.sketchapp.domain.usecase.auth.*
import com.hotmail.arehmananis.sketchapp.domain.usecase.drawing.*
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.*
import com.hotmail.arehmananis.sketchapp.presentation.AuthViewModel
import com.hotmail.arehmananis.sketchapp.presentation.feature.auth.LoginViewModel
import com.hotmail.arehmananis.sketchapp.presentation.feature.drawing.DrawingViewModel
import com.hotmail.arehmananis.sketchapp.presentation.feature.gallery.GalleryViewModel
import com.hotmail.arehmananis.sketchapp.presentation.feature.home.HomeViewModel
import com.hotmail.arehmananis.sketchapp.presentation.feature.profile.ProfileViewModel
import com.hotmail.arehmananis.sketchapp.presentation.feature.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
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
    // Existing repositories
    single<UserRepository> { UserRepositoryImpl() }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }

    // New repositories for sketch app
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<SketchRepository> { SketchRepositoryImpl(get(), get(), get()) }
    single<DrawingRepository> { DrawingRepositoryImpl(androidContext()) }

    /**
     * Use case bindings
     */
    // Existing use cases
    factory { GetCurrentUserUseCase(get()) }
    factory { UpdateUserUseCase(get()) }
    factory { GetUserPreferencesUseCase(get()) }
    factory { UpdateUserPreferencesUseCase(get()) }

    // Auth use cases
    factory { SignInWithGoogleUseCase(get()) }
    factory { GetCurrentAuthUserUseCase(get()) }
    factory { SignOutUseCase(get()) }

    // Sketch use cases
    factory { GetUserSketchesUseCase(get()) }
    factory { GetSketchByIdUseCase(get()) }
    factory { CreateSketchUseCase(get()) }
    factory { UpdateSketchUseCase(get()) }
    factory { DeleteSketchUseCase(get()) }
    factory { SyncSketchesUseCase(get()) }

    // Drawing use cases
    factory { SaveDrawingUseCase(get()) }

    /**
     * ViewModel bindings
     */
    // Existing ViewModels
    viewModelOf(::HomeViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SettingsViewModel)

    // New ViewModels for sketch app
    viewModelOf(::AuthViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::GalleryViewModel)
    viewModelOf(::DrawingViewModel)
}
