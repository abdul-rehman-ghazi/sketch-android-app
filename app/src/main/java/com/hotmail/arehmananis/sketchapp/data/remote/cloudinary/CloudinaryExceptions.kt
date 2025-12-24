package com.hotmail.arehmananis.sketchapp.data.remote.cloudinary

/**
 * Cloudinary-specific exceptions
 * Pure Kotlin sealed class - KMP ready
 */
sealed class CloudinaryException(message: String) : Exception(message)

/**
 * Upload failed exception
 */
class CloudinaryUploadException(message: String) : CloudinaryException(message)

/**
 * Delete failed exception
 */
class CloudinaryDeleteException(message: String) : CloudinaryException(message)

/**
 * Configuration error exception
 */
class CloudinaryConfigException(message: String) : CloudinaryException(message)

/**
 * Network error exception
 */
class CloudinaryNetworkException(message: String) : CloudinaryException(message)
