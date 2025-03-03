package com.example.theanimalsarestarving.models

import com.google.gson.annotations.SerializedName
import org.bson.types.ObjectId
import java.util.Date

data class Pet(
    //@SerializedName("_id") val petId: String,  // Map _id from the backend to petId  ---- removed so that mongo assigns
    val name: String,
    val householdId: String? = null,
    val feedingTime: String,
    var fed: Boolean = false
)