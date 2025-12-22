package com.hotmail.arehmananis.sketchapp.di

import com.hotmail.arehmananis.sketchapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Koin module for network dependencies
 * Provides Ktor HttpClient configured for the app
 */
val networkModule = module {

    /**
     * Provides the base URL from BuildConfig
     * This allows easy testing and flexibility
     */
    single { provideBaseUrl() }

    /**
     * Provides Json instance for kotlinx.serialization
     * Configured to be lenient and ignore unknown keys
     */
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    /**
     * Provides Ktor HttpClient with common configuration
     * - Android engine for platform-specific networking
     * - Content negotiation with kotlinx.serialization
     * - Logging for debug builds
     * - Timeout configuration
     * - Default headers
     */
    single {
        HttpClient(Android) {
            // Base URL configuration
            defaultRequest {
                url(get<String>())
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            // JSON serialization
            install(ContentNegotiation) {
                json(get())
            }

            // Logging (only in debug builds)
            if (BuildConfig.ENABLE_LOGGING) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            println("Ktor: $message")
                        }
                    }
                    level = LogLevel.ALL
                }
            }

            // Timeout configuration
            engine {
                connectTimeout = 30_000
                socketTimeout = 30_000
            }
        }
    }
}

/**
 * Provides the base URL from BuildConfig
 */
private fun provideBaseUrl(): String = BuildConfig.BASE_URL