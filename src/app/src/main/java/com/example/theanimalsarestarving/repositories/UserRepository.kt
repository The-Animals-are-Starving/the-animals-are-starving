package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.models.UserRole
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

private const val TAG = "UserRepository"


object UserRepository {
    //singleton for current list of users
    var currUsers: List<User> = emptyList()

    suspend fun updateUserRole(userEmail: String, newRole: UserRole) {
        try {
            // Make the API call in the IO thread using withContext
            val response: Response<User> = withContext(Dispatchers.IO) {
                apiService.updateRoleManager(userEmail).execute() // This is a blocking call
            }

            // Handle the response
            if (response.isSuccessful) {
                // Log the successful response
                Log.d(TAG, "Role updated successfully for $userEmail: ${response.body()}")
            } else {
                // Log failure response
                Log.e(TAG, "Failed to update role for $userEmail: ${response.code()} ${response.message()}")
            }
        } catch (t: Throwable) {
            // Log any exception that occurred during the call
            Log.e(TAG, "Error updating role for $userEmail: ${t.message}")
        }
    }

    //BY HOUSEHOLD
    suspend fun fetchAllUsersFromDB(householdId: String) {
        try {
            val response: Response<List<User>> = withContext(Dispatchers.IO) {
                apiService.getAllUsers(householdId).execute() // This is a blocking call
            }

            if (response.isSuccessful) {
                currUsers = response.body() ?: emptyList()
                Log.d(TAG, "Fetched and stored pets: ${currUsers}")
            } else {
                Log.e(TAG, "Error: ${response.code()} ${response.message()}")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failure: ${t.message}")
        }
    }

}