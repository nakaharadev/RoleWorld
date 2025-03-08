package com.nakaharadev.roleworld.network.task

import com.nakaharadev.roleworld.App
import com.nakaharadev.roleworld.network.data.AbstractRequest
import com.nakaharadev.roleworld.network.data.AbstractResponse
import com.nakaharadev.roleworld.network.data.request.AuthRequest

class AuthTask(val mode: Int, val request: AbstractRequest?) : AbstractTask() {
    companion object {
        val SIGN_IN_MODE = 1
        val SIGN_UP_MODE = 2
    }

    override lateinit var callback: (AbstractResponse) -> Unit

    @Throws(Exception::class)
    override fun task() {
        if (request == null) return

        if (mode == SIGN_IN_MODE) {
            val response = App.networkApi.signIn(request as AuthRequest.SignIn).execute()
            callback(response.body()!!)
        } else {
            val response = App.networkApi.signUp(request as AuthRequest.SignUp).execute()
            callback(response.body()!!)
        }
    }
}