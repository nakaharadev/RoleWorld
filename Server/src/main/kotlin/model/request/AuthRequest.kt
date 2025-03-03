package com.nakaharadev.model.request

object AuthRequest {
    data class SignInRequest(
        val login: String,
        val password: String
    )

    data class SignUpRequest(
        val nickname: String,
        val login: String,
        val password: String
    )
}