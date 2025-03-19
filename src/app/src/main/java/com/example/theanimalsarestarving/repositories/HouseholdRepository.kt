package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

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
        Log.d(TAG, "Setting Current Household to : $household")
        currentHousehold = household
    }


    suspend fun createHousehold(householdName: String, managerEmail: String): Household? {
        val requestBody = mapOf(
            "householdName" to householdName,
            "managerEmail" to managerEmail
        )

        try {
            Log.d(TAG, "Attempting to create household with requestBody: $requestBody")

            val response: Response<Household> = withContext(Dispatchers.IO) {
                apiService.createHousehold(requestBody)
            }

            if (response.isSuccessful) {
                val household = response.body()
                Log.d(TAG, "createHousehold response body: $household")
                Log.d(TAG, "Response Body: $household")
                return household
            } else {
                Log.e(TAG, "Failed to create household: ${response.code()} ${response.message()}")
                return null
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException creating household: ${e.message}")
            return null
        } catch (e: IOException) {
            Log.e(TAG, "IOException creating household ${e.message}")
            return null
        }
    }
}
