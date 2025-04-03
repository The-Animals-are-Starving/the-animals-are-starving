package com.example.theanimalsarestarving.activities.helper

import com.example.theanimalsarestarving.network.ApiService
import com.example.theanimalsarestarving.network.NetworkManager
import com.example.theanimalsarestarving.network.RetrofitClient
import com.example.theanimalsarestarving.network.UserApiService
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