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
        when (newRole) {
            UserRole.ADMIN -> setManager(userEmail)
            UserRole.REGULAR -> setNormal(userEmail)
            UserRole.RESTRICTED -> setRestricted(userEmail)
            }
    }

    //this is very bad code duplication but its the only way i can get it to work

    private suspend fun setManager(userEmail: String) {
        try {
            // Make the API call in the IO thread using withContext
            val response: Response<User> = withContext(Dispatchers.IO) {
                apiService.updateRoleManager(userEmail).execute() // This is a blocking call
            }
            if (response.isSuccessful) {
                Log.d(TAG, "Role updated successfully for $userEmail: ${response.body()}")
            } else {
                Log.e(
                    TAG,
                    "Failed to update role for $userEmail: ${response.code()} ${response.message()}"
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error updating role for $userEmail: ${t.message}")
        }
    }

    private suspend fun setNormal(userEmail: String) {
        try {
            // Make the API call in the IO thread using withContext
            val response: Response<User> = withContext(Dispatchers.IO) {
                apiService.updateRoleNormal(userEmail).execute() // This is a blocking call
            }
            if (response.isSuccessful) {
                Log.d(TAG, "Role updated successfully for $userEmail: ${response.body()}")
            } else {
                Log.e(
                    TAG,
                    "Failed to update role for $userEmail: ${response.code()} ${response.message()}"
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error updating role for $userEmail: ${t.message}")
        }
    }

    private suspend fun setRestricted(userEmail: String) {
        try {
            // Make the API call in the IO thread using withContext
            val response: Response<User> = withContext(Dispatchers.IO) {
                apiService.updateRoleNormal(userEmail).execute() // This is a blocking call
            }
            if (response.isSuccessful) {
                Log.d(TAG, "Role updated successfully for $userEmail: ${response.body()}")
            } else {
                Log.e(
                    TAG,
                    "Failed to update role for $userEmail: ${response.code()} ${response.message()}"
                )
            }
        } catch (t: Throwable) {
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