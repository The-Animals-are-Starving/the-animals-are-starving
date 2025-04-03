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
        val userApiService = retrofit.create(UserApiService::class.java)
        val mainRepository = MainRepository(apiService, userApiService)
        NetworkManager.initialize(apiService, mainRepository, userApiService)
        return Pair(apiService, mainRepository)
    }
}