package com.example.theanimalsarestarving.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Base URL for your backend API
    private const val BASE_URL = "http://localhost:5001"  // Replace with your API base URL

    // Retrofit instance - created only once
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)  // Set the base URL for the API
            .addConverterFactory(GsonConverterFactory.create()) // Convert JSON to Kotlin objects
            .build() // Build the Retrofit instance
    }

    // ApiService instance - you use this to make API requests
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java) // Create the ApiService interface
    }

    val baseUrl: String
        get() = BASE_URL
}