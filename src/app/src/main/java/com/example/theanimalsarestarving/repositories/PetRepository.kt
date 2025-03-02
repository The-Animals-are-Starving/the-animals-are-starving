package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Pet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import com.example.theanimalsarestarving.network.NetworkManager.mainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val TAG = "FeedingActivity"

object PetRepository {
    var pets: List<Pet> = emptyList()
}

//suspend because we want this to finish before continuing
suspend fun fetchPets(householdId: String) {
    try {
        // Run the network request on an IO thread to avoid blocking the main thread
        val response: Response<List<Pet>> = withContext(Dispatchers.IO) {
            apiService.getPets(householdId).execute() // This is a blocking call
        }

        if (response.isSuccessful) {
            PetRepository.pets = response.body() ?: emptyList()
            Log.d("PetRepository", "Fetched and stored pets: ${PetRepository.pets}")
        } else {
            Log.e("PetRepository", "Error: ${response.code()} ${response.message()}")
        }
    } catch (t: Throwable) {
        Log.e("PetRepository", "Failure: ${t.message}")
    }
}