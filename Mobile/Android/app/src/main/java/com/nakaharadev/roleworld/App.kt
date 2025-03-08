package com.nakaharadev.roleworld

import android.app.Application
import android.widget.Toast
import com.nakaharadev.roleworld.network.NetworkApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class App : Application() {
    companion object {
        lateinit var networkApi: NetworkApi
    }

    override fun onCreate() {
        super.onCreate()

        configureRetrofit()
    }

    private fun configureRetrofit() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.114:8080/app/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        networkApi = retrofit.create(NetworkApi::class.java)
    }
}