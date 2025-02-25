package com.example.theanimalsarestarving

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.FrameLayout

class RestrictedActivity : AppCompatActivity() {

    private lateinit var petContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restricted_activity)

        // Get the container where pet items will be added
        petContainer = findViewById(R.id.petContainer)

        // Add a default pet when the activity is created
        addPet("Stinky Dog", "by PERSON at TIME", R.drawable.dog_default_icon)

    }

    // Method to add a pet to the container dynamically
    private fun addPet(petName: String, feedingInfo: String, petImageResId: Int) {
        // Inflate the pet_item layout
        val petLayout = LayoutInflater.from(this).inflate(R.layout.pet_item, petContainer, false)

        // Set the pet image
        val petImage: ImageView = petLayout.findViewById(R.id.pet_image)
        petImage.setImageResource(petImageResId)

        // Set the pet name
        val petNameText: TextView = petLayout.findViewById(R.id.pet_name)
        petNameText.text = petName

        // Set feeding info
        val feedingInfoText: TextView = petLayout.findViewById(R.id.feeding_info)
        feedingInfoText.text = feedingInfo

        // Set fed status (this can be dynamic too)
        val fedStatusText: TextView = petLayout.findViewById(R.id.fed_status)
        fedStatusText.text = "FED"

        // Add the new pet layout to the container
        petContainer.addView(petLayout)
    }
}
