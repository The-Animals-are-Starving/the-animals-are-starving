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

    fun setCurrUser(user: User){
        currUser = user
    }

    suspend fun fetchCurrUser(userEmail: String): User? {
        return try {
            // Switch to the IO dispatcher for the blocking network call
            val response: Response<User> = withContext(Dispatchers.IO) {
                apiService.getUser(userEmail).execute() // This is still a blocking call
            }

            if (response.isSuccessful) {
                val user = response.body()
                if (user == null) {
                    Log.d("CurrUserRepository", "User Does Not Exist In DB: $userEmail")
                } else {
                    Log.d("CurrUserRepository", "FetchedUser: $user")
                }
                user
            } else {
                Log.e("CurrUserRepository", "Error: ${response.code()} ${response.message()}")
                null
            }
        } catch (t: Throwable) {
            Log.e("CurrUserRepository", "WE ARE HERE")
            Log.e("CurrUserRepository", "Failure: ${t.message}")
            null
        }
    }
}