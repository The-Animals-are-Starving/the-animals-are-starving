package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

object CurrUserRepository {
    private const val TAG = "CurrUserRepository"

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
            val response: Response<User> = withContext(Dispatchers.IO) {
                apiService.getUser(userEmail).execute() // Blocking call to fetch the user
            }

            if (response.isSuccessful) {
                val user = response.body()
                if (user==null) {
                    Log.d(TAG, "User Does Not Exist In DB: $userEmail")
                    null
                } else {
                    Log.d(TAG, "Fetched User: $user")
                    user
                }
            } else {
                Log.e(TAG, "Error: ${response.code()} ${response.message()}")
                null
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException in FetchCurrUser: ${e.message()}")
            null
        } catch (e: IOException) {
            Log.e(TAG, "IOException in FetchCurrUser: ${e.message}")
            null
        }
    }
}