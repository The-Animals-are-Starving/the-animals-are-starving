package com.example.theanimalsarestarving.repositories

import android.util.Log
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
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user: ${e.message}")
            null
        }
    }

    suspend fun updateUserHouseholdId(email: String,householdId: String) {
        val requestBody = mapOf(
            "email" to email,
            "householdId" to householdId
        )
        try {
            Log.d(TAG, "Attempting to UPDATE user's household ID with requestBody: $requestBody")

            // Make the network request asynchronously using enqueue on Dispatchers.IO
            val response = withContext(Dispatchers.IO) {
                // Execute the request synchronously using execute()
                apiService.updateUserHouseholdId(email, requestBody).execute()
            }

            if (response.isSuccessful) {
                    Log.d(TAG, "User updated successfully: ${response.body()}")

            } else {
                // If the request failed, log the error and return null
                Log.e(TAG, "Failed to updated user's householdId: ${response.code()} ${response.message()} ")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user's householdId: ${e.message}")
        }
    }

    suspend fun addUserToHousehold(householdId: String, email: String): User? {
        val requestBody = mapOf(
            "householdId" to householdId,
            "email" to email
        )

        return try {
            Log.d(TAG, "Attempting to add user to household with requestBody: $requestBody")

            // Make the network request asynchronously using enqueue on Dispatchers.IO
            val response = withContext(Dispatchers.IO) {
                // Execute the request synchronously using execute()
                apiService.addUserToHousehold(requestBody).execute()
            }

            if (response.isSuccessful) {
                // If the request was successful, return the user object from the response
                response.body().also {
                    Log.d(TAG, "User added successfully: $it")
                }
            } else {
                // If the request failed, log the error and return null
                Log.e(TAG, "Failed to add user to household: ${response.code()} ${response.message()} ")
                null
            }
        } catch (e: Exception) {
            // Catch any exceptions and log them
            Log.e(TAG, "Error adding user: ${e.message}")
            null
        }
    }




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