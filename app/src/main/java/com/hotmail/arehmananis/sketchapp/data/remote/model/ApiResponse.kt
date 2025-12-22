package com.hotmail.arehmananis.sketchapp.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic API response wrapper
 * Handles common response structure from backend
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("data")
    val data: T? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("success")
    val success: Boolean = false,

    @SerialName("error_code")
    val errorCode: String? = null
)

/**
 * Paginated API response
 */
@Serializable
data class PaginatedResponse<T>(
    @SerialName("data")
    val data: List<T> = emptyList(),

    @SerialName("page")
    val page: Int = 1,

    @SerialName("page_size")
    val pageSize: Int = 20,

    @SerialName("total_pages")
    val totalPages: Int = 1,

    @SerialName("total_items")
    val totalItems: Int = 0
)