package com.example.theanimalsarestarving.network

import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.models.User
import org.bson.types.ObjectId
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    //router.get("/:email", getUser);
    @GET("user/{email}")
    fun getUser(@Path("email") email: String): Call<User>  // Single user response

    /*TODO: UNTESTED
    //router.get("/household/:householdId", getPetsByHousehold);
    @GET("household/{householdId}")
    fun getPets(@Path("householdId") householdId: ObjectId): Call<Pet>  // All household pets

    //router.patch("/:petId/feed", updatePetFeedingStatus);
    @PATCH("pet/{petId}/feed")
    fun feedPet(
        @Path("petId") petId: ObjectId,      // Pet ID to identify the pet
        @Body fedStatus: Map<String, Boolean> // Directly passing the fed status in the request body
    ): Call<Pet>
    */

}
