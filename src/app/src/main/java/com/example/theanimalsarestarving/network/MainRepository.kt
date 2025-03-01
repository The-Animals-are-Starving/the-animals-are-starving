package com.example.theanimalsarestarving.network

import android.util.Log
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.models.User
import org.bson.types.ObjectId
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

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

    /* TODO: UNTESTED
    //    @GET("household/{householdId}")
    //    fun getPet(@Path("householdId") householdId: ObjectId): Call<Pet>  // all household pets response
    fun getPets(householdId: ObjectId, callback: (Pet?) -> Unit) {
        // Make the API call asynchronously
        apiService.getPets(householdId).enqueue(object : retrofit2.Callback<Pet> {
            override fun onResponse(call: Call<Pet>, response: Response<Pet>) {
                if (response.isSuccessful) {
                    val pet = response.body()
                    callback(pet)  // Return the pet through the callback
                    Log.d("MainRepository", "Success: ${response.body()}")  // Log the successful response
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")  // Log error responses
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Pet>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")  // Log failure due to network or other issues
                callback(null)
            }
        })
    }

    fun feedPet(petId: ObjectId, callback: (Pet?) -> Unit) {
        // Always send "fed" as true
        val fedStatus = mapOf("fed" to true)

        // Make the API call asynchronously
        apiService.feedPet(petId, fedStatus).enqueue(object : retrofit2.Callback<Pet> {
            override fun onResponse(call: Call<Pet>, response: Response<Pet>) {
                if (response.isSuccessful) {
                    val pet = response.body()
                    callback(pet)  // Return the pet through the callback
                    Log.d("MainRepository", "Success: ${response.body()}")  // Log the successful response
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")  // Log error responses
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Pet>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")  // Log failure due to network or other issues
                callback(null)
            }
        })
    }
    */

}
