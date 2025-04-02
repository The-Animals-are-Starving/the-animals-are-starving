package com.example.theanimalsarestarving.repositories

import android.util.Log
import com.example.theanimalsarestarving.models.Anomaly
import com.example.theanimalsarestarving.models.FeedingAnomalyResponse
import com.example.theanimalsarestarving.models.FeedingLog
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.network.ApiService
import com.example.theanimalsarestarving.network.UserApiService
import retrofit2.Call
import retrofit2.Response

class MainRepository(private val apiService: ApiService, private val userApiService: UserApiService) {
    fun getAllUsers(householdId: String, callback: (List<User>?) -> Unit) {
        Log.d("MainRepository", "Fetching users from household: $householdId")
        userApiService.getAllUsers(householdId).enqueue(object : retrofit2.Callback<List<User>> {
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
        user.householdId = HouseholdRepository.getCurrentHousehold()?._id.toString()
        userApiService.addUser(user).enqueue(object : retrofit2.Callback<User> { // this creates the user
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


    fun updateUserRole(email: String, newRole: String, callback: (Boolean) -> Unit) {
        val reqBody = mapOf(
            "email" to email,
            "role" to newRole
            )
        userApiService.updateUserRole(email, reqBody).enqueue(object : retrofit2.Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                callback(response.isSuccessful)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("MainRepository", "Error updating user role: ${t.message}")
                callback(false)
            }
        })
    }

    fun updateUserToken(email: String, token: String, callback: (Boolean) -> Unit) {
        val reqBody = mapOf("FCMToken" to token)
        userApiService.updateUserToken(email, reqBody).enqueue(object : retrofit2.Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                callback(response.isSuccessful)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("MainRepository", "Error updating user token: ${t.message}")
                callback(false)
            }
        })
    }


    fun getAllPets(householdId: String, callback: (List<Pet>?) -> Unit) {
        apiService.getAllPets(householdId).enqueue(object: retrofit2.Callback<List<Pet>> {
            override fun onResponse(call: Call<List<Pet>>, response: Response<List<Pet>>) {
                if (response.isSuccessful) {
                    val pets = response.body()
                    callback(pets)

                    Log.d("MainRepository", "Pets fetched successfully: $pets")
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<List<Pet>>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")
                callback(null)
            }
        })
    }

    fun addPet(pet: Pet, callback: (Pet?) -> Unit) {
        val body = mapOf(
            "name" to pet.name,
            "householdId" to pet.householdId,
            "feedingTime" to pet.feedingTime
        )
        apiService.addPet(body).enqueue(object : retrofit2.Callback<Pet> { // this creates the Pet
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
                callback(null)   //Return null in case of failure
            }
        })

    }

    fun getLogs(householdId: String, callback: (List<FeedingLog>?) -> Unit) {
        apiService.getLogs(householdId).enqueue(object: retrofit2.Callback<List<FeedingLog>> {
            override fun onResponse(call: Call<List<FeedingLog>>, response: Response<List<FeedingLog>>) {
                if (response.isSuccessful) {
                    val logs = response.body()
                    callback(logs)
    
                    Log.d("MainRepository", "Logs fetched successfully: $logs")
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")
                    callback(null)
                }
            }
            override fun onFailure(call: Call<List<FeedingLog>>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")
                callback(null)
            }
        })
    }


    fun getFeedingAnomalies(householdId: String, callback: (List<Anomaly>?) -> Unit) {
        Log.d("Anomalies", "Requesting anomaly from household: $householdId")

        apiService.getAnomalies(householdId).enqueue(object : retrofit2.Callback<FeedingAnomalyResponse> {
            override fun onResponse(call: Call<FeedingAnomalyResponse>, response: Response<FeedingAnomalyResponse>) {
                if (response.isSuccessful) {
                    val anomalies = response.body()?.anomalies
                    callback(anomalies)

                    Log.d("MainRepository", "Feeding anomalies fetched successfully: $anomalies")
                } else {
                    Log.e("MainRepository", "Error: ${response.code()} ${response.message()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<FeedingAnomalyResponse>, t: Throwable) {
                Log.e("MainRepository", "Failure: ${t.message}")
                callback(null)
            }
        })
    }



}
