package com.example.edgecare

import com.example.edgecare.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder()
    .baseUrl("http://10.15.94.116:8000/")
    .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
    .build()

val apiService = retrofit.create(ApiService::class.java)
