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

    fun addUser(user: User, callback: (User?) -> Unit) {
        apiService.addUser(user).enqueue(object : retrofit2.Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val newUser = response.body()
                    callback(newUser)  // Return the new user through the callback
                    Log.d("MainRepository", "User added successfully: ${response.body()}")
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")
                    callback(null)  // Return null in case of failure
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")
                callback(null)  // Return null in case of failure
            }
        })
    }

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

    //    @GET("household/{householdId}")
    //    fun getPet(@Path("householdId") householdId: ObjectId): Call<Pet>  // all household pets response
// Update your method signature to expect a list of pets
    fun getPets(householdId: ObjectId, callback: (List<Pet>?) -> Unit) {
        // Make the API call asynchronously
        apiService.getPets(householdId).enqueue(object : retrofit2.Callback<List<Pet>> {
            override fun onResponse(call: Call<List<Pet>>, response: Response<List<Pet>>) {
                if (response.isSuccessful) {
                    // Log the raw response body as a string (before parsing it)
                    // Nah dawg fuck that android crashed dont like that shit
                    /*val rawJson = response.raw().body()?.string() // Get the raw JSON string
                    Log.d("MainRepository", "Raw JSON response: $rawJson")*/

                    // Now parse the response as usual
                    val pets = response.body()
                    callback(pets)  // Return the list of pets through the callback

                    // Log the parsed response body
                    Log.d("MainRepository", "Parsed response: $pets")
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")  // Log error responses
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<Pet>>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")  // Log failure due to network or other issues
                callback(null)
            }
        })
    }



    /* TODO: UNTESTED

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
