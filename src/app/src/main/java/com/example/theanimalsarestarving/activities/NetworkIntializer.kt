package com.example.theanimalsarestarving.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.theanimalsarestarving.repositories.MainRepository

object NetworkInitializer {
    fun init(): Pair<ApiService, MainRepository> {
        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitClient.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val mainRepository = MainRepository(apiService)
        // Initialize your network manager or any other singletons here
        NetworkManager.initialize(apiService, mainRepository)
        return Pair(apiService, mainRepository)
    }
}