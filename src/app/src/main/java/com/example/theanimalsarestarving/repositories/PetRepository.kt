package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.FeedingLog
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response


private const val TAG = "PetRepository"

object PetRepository {
    //singleton for current list of pets
    var currPets: List<Pet> = emptyList()


    fun getPets(): List<Pet> {
        return currPets
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
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException fetching pets from database: ${e.message}")
        }
    }

    fun logFeed(petName: String, userEmail: String, callback: (Boolean) -> Unit) {
        val houseId = HouseholdRepository.getCurrentHousehold()?._id.toString()
        val body = mapOf(
            "userEmail" to userEmail,
            "householdId" to houseId)
        Log.d("PetRepository", "Attempting to log feeding: $petName, $userEmail, $houseId")
        apiService.logFeed(petName, body).enqueue(object : retrofit2.Callback<FeedingLog> {
            override fun onResponse(call: Call<FeedingLog>, response: Response<FeedingLog>) {
                callback(response.isSuccessful)
            }

            override fun onFailure(call: Call<FeedingLog>, t: Throwable) {
                Log.d("PetRepository", "Error logging feeding: ${t.message}")
                callback(false)
            }
        })
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
    }
}
