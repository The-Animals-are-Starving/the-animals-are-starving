package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.models.User
import retrofit2.Response
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call


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
                apiService.getAllPets(householdId).execute() // This is a blocking call
            }

            if (response.isSuccessful) {
                currPets = response.body() ?: emptyList()
                Log.d(TAG, "Fetched and stored pets: ${currPets}")
            } else {
                Log.e(TAG, "Error: ${response.code()} ${response.message()}")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failure: ${t.message}")
        }
    }


    fun feedPet(petName: String, callback: (Boolean) -> Unit) {
        Log.d("PetRepository", "Attempting to feed pet name: $petName")
        apiService.feedPet(petName).enqueue(object : retrofit2.Callback<Pet> {
            override fun onResponse(call: Call<Pet>, response: Response<Pet>) {
                callback(response.isSuccessful)
            }

            override fun onFailure(call: Call<Pet>, t: Throwable) {
                Log.e("PetRepository", "Error feeding pet: ${t.message}")
                callback(false)
            }
        })

        /*try {
            val response: Response<Pet> = withContext(Dispatchers.IO) {
                apiService.feedPet(petName).execute() // This is a blocking call
            }

            if (response.isSuccessful) {
                Log.d(TAG, "feedPet Response: ${response.code()} ${response.message()}")
                val petToUpdate = currPets.find { it.name == petName } //TODO: Is this necessary?
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
        }*/
    }

    //removed for the same reason, find pets through household search
    /*suspend fun addPetToHousehold(requestBody: Map<String, String>): Boolean {
         try {
            Log.d(TAG, "Attempting to add pet: $requestBody")
            val response: Response<Pet> = withContext(Dispatchers.IO) {
                apiService.addPetToHousehold(requestBody)
                    .execute() // Execute the Call synchronously to get a Response
            }

            if (response.isSuccessful) {
                Log.d(TAG, "Pet Added")
                return true
//                Log.d(TAG, "Response addPetToHousehold: ${response.toString()}")
//                val pet = response.body() // Assuming the response is a Pet object
//                if (pet != null) {
//                    Log.d(TAG, "Pet added: $pet") //TODO: broken log
//                    pet
//                } else {
//                    Log.e(TAG, "Pet is null in the response")
//                    null
//                }
            } else {
                Log.e(TAG, "Failed to add pet: ${response.code()} ${response.message()}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding pet: ${e.message}")
            return false
        }
    }*/

}
