package com.example.theanimalsarestarving.network

import com.example.theanimalsarestarving.models.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Define the endpoint to fetch a single user by email
    @GET("user/{email}")
    fun getUser(@Path("email") email: String): Call<User>  // Single user response
}
