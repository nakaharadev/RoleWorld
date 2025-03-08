package com.nakaharadev.model.response

data class AuthResponse(
    val status: Int,
    val statusStr: String,
    val nickname: String,
    val id: String
)