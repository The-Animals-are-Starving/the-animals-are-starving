package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.models.UserRole
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import com.example.theanimalsarestarving.network.NetworkManager.userApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

private const val TAG = "UserRepository"

object UserRepository {

    suspend fun createUser(email: String, name: String, householdId: String): User? {
        val requestBody = mapOf(
            "email" to email,
            "name" to name,
            "householdId" to householdId
        )

        return try {
            Log.d(TAG, "Attempting to create user with requestBody: $requestBody")
            val response = withContext(Dispatchers.IO) {
                apiService.createUser(requestBody).execute()  // Perform the request synchronously
            }

            if (response.isSuccessful) {
                response.body()  // Return the created user if successful
            } else {
                Log.e(TAG, "Failed to create user: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException creating user: ${e.message()}")
            null
        } catch (e: IOException) {
            Log.e(TAG, "IOException creating user ${e.message}")
            null
        }
    }

    suspend fun updateUserHouseholdId(email: String, householdId: String) {
        val requestBody = mapOf(
            "email" to email,
            "householdId" to householdId
        )
        try {
            Log.d(TAG, "Attempting to UPDATE user's household ID with requestBody: $requestBody")

            // Make the network request asynchronously using enqueue on Dispatchers.IO
            val response = withContext(Dispatchers.IO) {
                // Execute the request synchronously using execute()
                userApiService.updateUserHouseholdId(email, requestBody).execute()
            }

            if (response.isSuccessful) {
                Log.d(TAG, "User updated successfully: ${response.body()}")
            } else {
                Log.e(TAG, "Failed to updated user's householdId: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException updating user's householdId:: ${e.message()}")
        } catch (e: IOException) {
            Log.e(TAG, "IOException updating user's householdId: ${e.message}")
        }
    }

    suspend fun updateUserRole(userEmail: String, newRole: UserRole) {
        when (newRole) {
            UserRole.ADMIN -> setManager(userEmail)
            UserRole.REGULAR -> setNormal(userEmail)
            UserRole.RESTRICTED -> setRestricted(userEmail)
        }
    }

    private suspend fun setManager(userEmail: String) {
        try {
            // Make the API call in the IO thread using withContext
            val response: Response<User> = withContext(Dispatchers.IO) {
                userApiService.updateRoleManager(userEmail).execute() // This is a blocking call
            }
            if (response.isSuccessful) {
                Log.d(TAG, "Role updated successfully for $userEmail: ${response.body()}")
            } else {
                Log.e(TAG, "Failed to update role for $userEmail: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Error ${e.code()} updating role for $userEmail: ${e.message()}")
        } catch (e: IOException) {
            Log.e(TAG, "IOException updating role for $userEmail: ${e.message}")
        }
    }

    private suspend fun setNormal(userEmail: String) {
        try {
            // Make the API call in the IO thread using withContext
            val response: Response<User> = withContext(Dispatchers.IO) {
                userApiService.updateRoleNormal(userEmail).execute() // This is a blocking call
            }
            if (response.isSuccessful) {
                Log.d(TAG, "Role updated successfully for $userEmail: ${response.body()}")
            } else {
                Log.e(TAG, "Failed to update role for $userEmail: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Error ${e.code()} updating role for $userEmail: ${e.message()}")
        } catch (e: IOException) {
            Log.e(TAG, "IOException updating role for $userEmail: ${e.message}")
        }
    }

    private suspend fun setRestricted(userEmail: String) {
        try {
            // Make the API call in the IO thread using withContext
            val response: Response<User> = withContext(Dispatchers.IO) {
                userApiService.updateRoleRestricted(userEmail).execute() // This is a blocking call
            }
            if (response.isSuccessful) {
                Log.d(TAG, "Role updated successfully for $userEmail: ${response.body()}")
            } else {
                Log.e(TAG, "Failed to update role for $userEmail: ${response.code()} ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Error ${e.code()} updating role for $userEmail: ${e.message()}")
        } catch (e: IOException) {
            Log.e(TAG, "IOException updating role for $userEmail: ${e.message}")
        }
    }
}
