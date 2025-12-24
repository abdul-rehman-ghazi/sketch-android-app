package com.hotmail.arehmananis.sketchapp.data.remote.cloudinary

/**
 * Cloudinary configuration
 * Pure Kotlin data class - KMP ready
 *
 * @param cloudName Cloudinary cloud name from dashboard
 * @param apiKey Cloudinary API key from dashboard
 * @param apiSecret Cloudinary API secret from dashboard
 * @param secure Use HTTPS for all requests (default: true)
 */
data class CloudinaryConfig(
    val cloudName: String,
    val apiKey: String,
    val apiSecret: String,
    val secure: Boolean = true
)
