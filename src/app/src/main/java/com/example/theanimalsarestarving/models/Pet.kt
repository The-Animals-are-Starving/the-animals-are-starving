package com.example.theanimalsarestarving.models

data class Pet(
    //@SerializedName("_id") val petId: String,  // Map _id from the backend to petId  ---- removed so that mongo assigns
    val name: String,
    val householdId: String? = null,
    val feedingTime: String,
    var fed: Boolean = false
)