package com.example.theanimalsarestarving.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.theanimalsarestarving.R

class FeedingActivity : AppCompatActivity() {

    private var TAG = "Feeding Activity"
    private lateinit var petContainer: LinearLayout
    private lateinit var undoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feeding_activity)

        petContainer = findViewById(R.id.petContainer)
        undoButton = findViewById(R.id.undo_button)

        //TODO: FETCH THIS INFO FROM BACKEND
        val petName = "stinky dog"
        val feedingInfo = "by PERSON at TIME"
        val petImage = R.drawable.dog_default_icon
        val isFed = false

        loadPet(petName, feedingInfo, petImage, isFed) //example

        undoButton.setOnClickListener{
            Log.d(TAG, "Undo Button Clicked")
        }
    }

    // Method to add a pet to the container dynamically
    private fun loadPet(petName: String, feedingInfo: String, petImageResId: Int, isFed: Boolean) {
        // Inflate the pet_item layout
        val petLayout = LayoutInflater.from(this).inflate(R.layout.pet_item, petContainer, false)

        // Set the pet image
        val petImage: ImageView = petLayout.findViewById(R.id.pet_image)
        petImage.setImageResource(petImageResId)

        // Set the pet name
        val petNameText: TextView = petLayout.findViewById(R.id.pet_name)
        petNameText.text = petName

        val petCircle: ImageView = petLayout.findViewById(R.id.pet_circle)
        val fedStatusText: TextView = petLayout.findViewById(R.id.fed_status)
        val feedingButton: Button = petLayout.findViewById(R.id.indicate_fed_button)
        val feedingInfo: TextView = petLayout.findViewById(R.id.feeding_info)


        if(isFed == false){
            // Set fed status (this can be dynamic too)
            fedStatusText.text = "NOT FED"
            petCircle.setColorFilter(ContextCompat.getColor(this, R.color.dark_pink))
            feedingButton.visibility = View.VISIBLE
            feedingInfo.visibility = View.GONE
        } else {
            fedStatusText.text = "FED"
            petCircle.setColorFilter(ContextCompat.getColor(this, R.color.base_green))
            feedingButton.visibility = View.GONE
            feedingInfo.visibility = View.VISIBLE
        }

        //TODO: I dont think this is dynamic? Send this to backend

        feedingButton.setOnClickListener {
            fedStatusText.text = "FED"
            petCircle.setColorFilter(ContextCompat.getColor(this, R.color.base_green))
            feedingButton.visibility = View.GONE
            feedingInfo.visibility = View.VISIBLE
            //TODO: isFed = true; update backend

        }

        // Add the new pet layout to the container
        petContainer.addView(petLayout)
    }
}
