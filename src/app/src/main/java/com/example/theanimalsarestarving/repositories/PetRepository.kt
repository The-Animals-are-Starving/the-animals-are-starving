package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Pet
import retrofit2.Response
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val TAG = "PetRepository"

object PetRepository {
    //singleton for current list of pets
    var currPets: List<Pet> = emptyList()


    fun getPets(): List<Pet> {
        return currPets;
    }

    suspend fun fetchPetsFromDB(householdId: String) {
        try {
            val response: Response<List<Pet>> = withContext(Dispatchers.IO) {
                apiService.getPets(householdId).execute() // This is a blocking call
            }

            if (response.isSuccessful) {
                PetRepository.currPets = response.body() ?: emptyList()
                Log.d(TAG, "Fetched and stored pets: ${PetRepository.currPets}")
            } else {
                Log.e(TAG, "Error: ${response.code()} ${response.message()}")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failure: ${t.message}")
        }
    }


    suspend fun feedPet(petId: String) {
        try {
            val response: Response<Pet> = withContext(Dispatchers.IO) {
                apiService.feedPet(petId).execute() // This is a blocking call
            }

            if (response.isSuccessful) {
                Log.d(TAG, "feedPet Response: ${response.code()} ${response.message()}")
                val petToUpdate = PetRepository.currPets.find { it.petId.toString() == petId } //TODO: Is this necessary?
                petToUpdate?.let {
                    it.fed = true  // Mark the pet as fed
                    Log.d(TAG, "Pet updated: ${it.name} is now fed.")
                }
            } else {
                Log.e(TAG, "Failed to update pet feeding status: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            // Log any errors that occur during the network request
            Log.e(TAG, "Error: ${e.message}")
        }
    }


}
