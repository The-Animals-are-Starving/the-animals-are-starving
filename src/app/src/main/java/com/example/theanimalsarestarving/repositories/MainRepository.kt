package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.network.ApiService
import retrofit2.Call
import retrofit2.Response

//TODO: MOVE THESE FUNCTIONS OUT OF MAINREPOSITORY
class MainRepository(private val apiService: ApiService) {


    fun getAllUsers(householdId: String, callback: (List<User>?) -> Unit) {
        apiService.getAllUsers(householdId).enqueue(object : retrofit2.Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body()
                    callback(users)
                    
                    Log.d("MainRepository", "Users fetched successfully: $users")
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")
                callback(null)
            }
        })
    }


    fun addUser(user: User, callback: (User?) -> Unit) {
        apiService.addUser(user).enqueue(object : retrofit2.Callback<User> { // this creates the user
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

    fun addUserToHousehold (user: User, householdId: String, callback: (User?) -> Unit) {
        val body = mapOf(
            "email" to user.email,
            "householdId" to householdId
        )
        apiService.addUserToHousehold(body).enqueue(object : retrofit2.Callback<User> { // this adds user to the household
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val newUser = response.body()
                    callback(newUser)  // Return the new user through the callback
                    Log.d("addUserToHousehold", "User added to household successfully: ${response.body()}")
                } else {
                    Log.e("addUserToHousehold", "Error: ${response.code()} ${response.message()}")
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

    fun addPet(pet: Pet, callback: (Pet?) -> Unit) {
        apiService.addPet(pet).enqueue(object : retrofit2.Callback<Pet> { // this creates the Pet
            override fun onResponse(call: Call<Pet>, response: Response<Pet>) {
                if (response.isSuccessful) {
                    val newPet = response.body()
                    callback(newPet)  // Return the new Pet through the callback
                    Log.d("MainRepository", "Pet added successfully: ${response.body()}")
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")
                    callback(null)  // Return null in case of failure
                }
            }

            override fun onFailure(call: Call<Pet>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")
                callback(null)  // Return null in case of failure
            }
        })

    }

    //    @GET("household/{householdId}")
    //    fun getPet(@Path("householdId") householdId: ObjectId): Call<Pet>  // all household pets response
// Update your method signature to expect a list of pets
    fun getPets(householdId: String, callback: (List<Pet>?) -> Unit) {
        // Make the API call asynchronously
        apiService.getPets(householdId).enqueue(object : retrofit2.Callback<List<Pet>> {
            override fun onResponse(call: Call<List<Pet>>, response: Response<List<Pet>>) {
                if (response.isSuccessful) {
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

}
