package com.example.theanimalsarestarving.network

import com.example.theanimalsarestarving.models.FeedingAnomalyResponse
import com.example.theanimalsarestarving.models.Anomaly
import com.example.theanimalsarestarving.models.FeedingLog
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.models.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("user")
    fun addUser(@Body user: User): Call<User>

    @POST("user")
    fun createUser(@Body body: Map<String, String>): Call<User>


    @POST("household/add-user")
    fun addUserToHousehold(@Body body: Map<String, String>): Call<User>


    @PATCH("user/{email}")
    fun updateRoleManager(
        @Path("email") email: String,
        @Body body: Map<String, String> = mapOf("role" to "manager")
    ): Call<User>

    @PATCH("user/{email}")
    fun updateRoleNormal(
        @Path("email") email: String,
        @Body body: Map<String, String> = mapOf("role" to "normal")
    ): Call<User>

    @PATCH("user/{email}")
    fun updateRoleRestricted(
        @Path("email") email: String,
        @Body body: Map<String, String> = mapOf("role" to "restricted")
    ): Call<User>

    @PATCH("user/{email}/")
    fun updateUserToken(
        @Path("email") email: String,
        @Body body: Map<String, String>
    ): Call<User>

    @PATCH("user/update-household/{email}")
    fun updateUserHouseholdId(
        @Path("email") email: String,
        @Body body: Map<String, String>
    ): Call<User>

    @GET("user/{householdId}")
    fun getAllUsers(@Path("householdId") householdId: String): Call<List<User>>

    @GET("user/specific-user/{email}")
    fun getUser(@Path("email") email: String): Call<User> // Single user response

    @PATCH("user/{email}")
    fun updateUserRole(@Path("email") email: String, @Body body: Map<String, String>): Call<User>  // Single user response

    @DELETE("user/{email}")
    fun deleteUser(@Path("email") email: String): Call<Boolean>

    @POST("pet")
    fun addPet(@Body body: Map<String, String?>): Call<Pet> //Adds pet to db

    @DELETE("pet/{petName}")
    fun deletePet(@Path("petName") petName: String): Call<Boolean>

    @GET("pet/{householdId}")
    fun getAllPets(@Path("householdId") householdId: String): Call<List<Pet>>  // Return a list of pets

    @PATCH("pet/{petName}/feed")
    fun feedPet(
        @Path("petName") petName: String,
        @Body body: Map<String, Boolean> = mapOf("fed" to true)
    ): Call<Pet>

    @POST("log/{petName}")
    fun logFeed(
        @Path("petName") petName: String,
        @Body body: Map<String,String>
    ): Call<FeedingLog>


    @POST("household/create")
    suspend fun createHousehold(
        @Body body: Map<String, String>
    ): Response<Household>

    @GET("log/household/{householdId}")
    fun getLogs(@Path("householdId") householdId: String): Call<List<FeedingLog>>

    @POST("notify/{email}")
    fun sendNotification(@Path("email") email: String): Call<Void>

    @POST("analytics/anomalies/{householdId}")
    fun getAnomalies(@Path("householdId") householdId: String): Call<FeedingAnomalyResponse>


}
