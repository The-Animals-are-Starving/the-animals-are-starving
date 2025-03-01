package com.example.theanimalsarestarving.network

//allows apiService and mainRepository to be accessed throughout different activities
object NetworkManager {
    // Singleton instances for ApiService and MainRepository
    lateinit var apiService: ApiService
    lateinit var mainRepository: MainRepository

    // Initialize the singleton with instances of ApiService and MainRepository
    fun initialize(apiService: ApiService, mainRepository: MainRepository) {
        this.apiService = apiService
        this.mainRepository = mainRepository
    }

    // Optional: If you want to check if it has been initialized
    fun isInitialized(): Boolean {
        return ::apiService.isInitialized && ::mainRepository.isInitialized
    }
}
