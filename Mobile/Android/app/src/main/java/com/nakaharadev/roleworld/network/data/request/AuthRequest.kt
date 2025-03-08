package com.nakaharadev.roleworld.network.data.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.nakaharadev.roleworld.network.data.AbstractRequest

object AuthRequest {
    data class SignIn(
        @SerializedName("login")
        @Expose
        val login: String,

        @SerializedName("password")
        @Expose
        val password: String
    ) : AbstractRequest()

    data class SignUp(
        @SerializedName("nickname")
        @Expose
        val nickname: String,

        @SerializedName("login")
        @Expose
        val login: String,

        @SerializedName("password")
        @Expose
        val password: String
    ) : AbstractRequest()
}