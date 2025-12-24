package com.hotmail.arehmananis.sketchapp.di

import com.hotmail.arehmananis.sketchapp.BuildConfig
import com.hotmail.arehmananis.sketchapp.data.remote.cloudinary.CloudinaryConfig
import com.hotmail.arehmananis.sketchapp.data.remote.cloudinary.CloudinaryDataSource
import com.hotmail.arehmananis.sketchapp.data.remote.cloudinary.CloudinaryDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for Cloudinary dependencies
 */
val cloudinaryModule = module {

    /**
     * Cloudinary configuration from BuildConfig
     * Credentials are loaded from local.properties per flavor
     */
    single {
        CloudinaryConfig(
            cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME,
            apiKey = BuildConfig.CLOUDINARY_API_KEY,
            apiSecret = BuildConfig.CLOUDINARY_API_SECRET,
            secure = true  // Always use HTTPS
        )
    }

    /**
     * Cloudinary data source implementation
     * Singleton to reuse MediaManager instance
     */
    single<CloudinaryDataSource> {
        CloudinaryDataSourceImpl(
            context = androidContext(),
            config = get()
        )
    }
}
