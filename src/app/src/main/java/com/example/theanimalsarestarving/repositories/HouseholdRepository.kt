package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

object HouseholdRepository {

    // Singleton for current household
    private var currentHousehold: Household? = null

    private const val TAG = "HouseholdRepository"

    // Getter for current household
    fun getCurrentHousehold(): Household? {
        return currentHousehold
    }

    // Setter for current household
    fun setCurrentHousehold(household: Household) {
        currentHousehold = household
    }

    suspend fun createHousehold(requestBody: Map<String, String>) {
        try {
            val response: Response<Household> = withContext(Dispatchers.IO) {
                apiService.createHousehold(requestBody) // Make the network request
            }

            if (response.isSuccessful) {
                val household = response.body()
                if (household != null) {
                    // Set the current household in the singleton
                    setCurrentHousehold(household)
                    Log.d(TAG, "Response Body: $household")
                }
            } else {
                Log.e(TAG, "Failed to create household: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating household: ${e.message}")
        }
    }
}
