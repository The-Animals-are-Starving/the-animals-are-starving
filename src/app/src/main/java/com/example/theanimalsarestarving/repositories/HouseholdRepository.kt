package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

object HouseholdRepository {

    private const val TAG = "HouseholdRepository"


    suspend fun createHousehold(requestBody: Map<String, String>) {
        try {
            val response: Response<Household> = withContext(Dispatchers.IO) {
                apiService.createHousehold(requestBody) // Make the network request
            }

            if (response.isSuccessful) {
                Log.d(TAG, "Response Body: ${response.body().toString()}")
            } else {
                Log.e(TAG, "Failed to create household: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating household: ${e.message}")
        }
    }
}

