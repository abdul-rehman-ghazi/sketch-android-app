package com.hotmail.arehmananis.sketchapp.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val bio: String,
    val avatarUrl: String? = null
)
