package com.example.edgecare

import com.example.edgecare.api.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(3, TimeUnit.MINUTES)
    .readTimeout(3, TimeUnit.MINUTES)
    .writeTimeout(3, TimeUnit.MINUTES)
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("http://10.15.94.116:8000/")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
    .build()

val apiService = retrofit.create(ApiService::class.java)
