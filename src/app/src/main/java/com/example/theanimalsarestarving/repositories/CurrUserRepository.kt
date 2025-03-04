package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

object CurrUserRepository {

    private var currUser: User? = null

    fun getCurrUser(): User? {
        return currUser
    }

    fun setCurrUser(user: User) {
        currUser = user
    }

    suspend fun fetchCurrUser(userEmail: String): User? {
        return try {
            // Switch to the IO dispatcher for the blocking network call
            val response: Response<List<User>> = withContext(Dispatchers.IO) {
                apiService.getUser(userEmail).execute() // Blocking call to fetch the user
            }

            if (response.isSuccessful) {
                val users = response.body()
                if (users.isNullOrEmpty()) {
                    Log.d("CurrUserRepository", "User Does Not Exist In DB: $userEmail")
                    null
                } else {
                    val user = users.find { it.email == userEmail }
                    Log.d("CurrUserRepository", "Fetched User: $user")
                    user // Return the first user matching the email (or null if not found)
                }
            } else {
                Log.e("CurrUserRepository", "Error: ${response.code()} ${response.message()}")
                null // Return null if the request was unsuccessful
            }
        } catch (t: Throwable) {
            Log.e("CurrUserRepository", "Failure in FetchCurrUser: ${t.message}")
            null // Return null in case of an exception
        }
    }
}