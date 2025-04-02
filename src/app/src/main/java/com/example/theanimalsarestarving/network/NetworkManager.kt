package com.example.theanimalsarestarving.network

import com.example.theanimalsarestarving.repositories.MainRepository

//allows apiService and mainRepository to be accessed throughout different activities
object NetworkManager {
    // Singleton instances for ApiService and MainRepository
    lateinit var apiService: ApiService
    lateinit var mainRepository: MainRepository
    lateinit var userApiService: UserApiService

    // Initialize the singleton with instances of ApiService and MainRepository
    fun initialize(apiService: ApiService, mainRepository: MainRepository, userApiService: UserApiService) {
        this.apiService = apiService
        this.mainRepository = mainRepository
        this.userApiService = userApiService
    }

    // Optional: If you want to check if it has been initialized
    fun isInitialized(): Boolean {
        return ::apiService.isInitialized && ::mainRepository.isInitialized
    }
}
