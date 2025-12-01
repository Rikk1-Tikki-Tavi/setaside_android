package com.example.setaside.data.remote

import android.content.Context
import com.example.setaside.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://setaside.matthewswong.tech/api/v1/"
    
    private var tokenManager: TokenManager? = null
    
    fun init(context: Context) {
        tokenManager = TokenManager(context)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val authInterceptor = Interceptor { chain ->
        val token = tokenManager?.let { 
            runBlocking { it.accessToken.first() }
        }
        
        val request = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            request.addHeader("Authorization", "Bearer $token")
        }
        request.addHeader("Content-Type", "application/json")
        chain.proceed(request.build())
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
