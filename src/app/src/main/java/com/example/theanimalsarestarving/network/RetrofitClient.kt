package com.example.theanimalsarestarving.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Base URL for your backend API
    private const val BASE_URL = "http://ec2-44-246-18-108.us-west-2.compute.amazonaws.com/"

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