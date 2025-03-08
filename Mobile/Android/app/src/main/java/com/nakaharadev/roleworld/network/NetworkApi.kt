package com.nakaharadev.roleworld.network


import com.nakaharadev.roleworld.network.data.request.AuthRequest
import com.nakaharadev.roleworld.network.data.response.AuthResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface NetworkApi {
    @POST("auth/sign_in")
    fun signIn(@Body body: AuthRequest.SignIn): Call<AuthResponse>

    @POST("auth/sign_up")
    fun signUp(@Body body: AuthRequest.SignUp): Call<AuthResponse>
}