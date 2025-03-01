package com.example.theanimalsarestarving.network

import android.util.Log
import com.example.theanimalsarestarving.models.User
import retrofit2.Call
import retrofit2.Response

class MainRepository(private val apiService: ApiService) {

    fun getUser(email: String, callback: (User?) -> Unit) {
        // Make the API call asynchronously
        apiService.getUser(email).enqueue(object : retrofit2.Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    callback(user)  // Return the user through callback
                    Log.d("MainRepository", "Success: ${response.body()}")  // Log the successful response
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")  // Log error responses
                    callback(null)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")  // Log failure due to network or other issues
                callback(null)
            }
        })
    }
}
