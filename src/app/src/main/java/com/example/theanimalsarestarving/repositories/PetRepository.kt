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


private const val TAG = "PetRepository"

object PetRepository {
    var pets: List<Pet> = emptyList()
}

//Suspend because we want this to finish before continuing
suspend fun fetchPets(householdId: String) {
    try {
        // Run the network request on an IO thread to avoid blocking the main thread
        val response: Response<List<Pet>> = withContext(Dispatchers.IO) {
            apiService.getPets(householdId).execute() // This is a blocking call
        }

        if (response.isSuccessful) {
            PetRepository.pets = response.body() ?: emptyList()
            Log.d(TAG, "Fetched and stored pets: ${PetRepository.pets}")
        } else {
            Log.e(TAG, "Error: ${response.code()} ${response.message()}")
        }
    } catch (t: Throwable) {
        Log.e(TAG, "Failure: ${t.message}")
    }
}


suspend fun feedPet(petId: String) {
    try {
        // Run the network request on an IO thread to avoid blocking the main thread
        val response: Response<Pet> = withContext(Dispatchers.IO) {
            apiService.feedPet(petId).execute() // This is a blocking call
        }

        if (response.isSuccessful) {
            Log.d(TAG, "feedPet Response: ${response.code()} ${response.message()}")

            // Find the pet in the repository's list of pets and update its fed status
            val petToUpdate = PetRepository.pets.find { it.petId.toString() == petId }
            petToUpdate?.let {
                it.fed = true  // Mark the pet as fed
                Log.d(TAG, "Pet updated: ${it.name} is now fed.")
            }
        } else {
            // Log failure if the response was not successful
            Log.e(
                TAG,
                "Failed to update pet feeding status: ${response.code()} ${response.message()}"
            )
        }
    } catch (e: Exception) {
        // Log any errors that occur during the network request
        Log.e(TAG, "Error: ${e.message}")
    }
}
