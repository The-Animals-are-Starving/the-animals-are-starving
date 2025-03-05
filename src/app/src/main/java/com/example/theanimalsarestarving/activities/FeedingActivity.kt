package com.example.theanimalsarestarving.activities

import android.app.AlertDialog
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
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.network.ApiService
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.network.NetworkManager
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.PetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FeedingActivity"
    }

    private lateinit var petContainer: LinearLayout
    private lateinit var mainRepository: MainRepository
    private lateinit var apiService: ApiService

    private val testHouseholdId: String = "67c2aa855a9890c0f183efa4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feeding_activity)

        petContainer = findViewById(R.id.petContainer)

        checkNetworkManager()

        Log.d(TAG, "Current Household: ${HouseholdRepository.getCurrentHousehold()}\n Current User: ${CurrUserRepository.getCurrUser()}\n Current pets: ${PetRepository.getPets()}")

        loadPets(HouseholdRepository.getCurrentHousehold()?._id.toString())
    }


    private fun loadPets(testHouseholdId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                PetRepository.fetchPetsFromDB(testHouseholdId)
                Log.d("TAG", "Pets Fetched: ${PetRepository.currPets}")

                val petImage = R.drawable.dog_default_icon

                for (pet in PetRepository.currPets) {
                    Log.d("TAG", "Pet: $pet")
                    loadPet(pet.name, pet.feedingTime, petImage, pet.fed)
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error fetching pets: ${e.message}")
            }
        }
    }

    // Method to add a pet to the container dynamically
    private fun loadPet(petName: String, feedingTime: String, petImageResId: Int, isFed: Boolean) {
        Log.d(TAG, "Loading pet: (petName: $petName, feedingTime: feedingTime, isFed: $isFed)")
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


        if (isFed == false) {
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

        val repository = PetRepository
        repository.feedPet(petName) { success ->

        }


        feedingButton.setOnClickListener {
            // Launch a coroutine in the main scope
            CoroutineScope(Dispatchers.Main).launch {
                // Call the suspend function feedPet inside the coroutine
                PetRepository.feedPet(petName) { success ->
                    if (success) {
                        // Update UI after feeding the pet
                        fedStatusText.text = "FED"
                        petCircle.setColorFilter(
                            ContextCompat.getColor(
                                this@FeedingActivity,
                                R.color.base_green
                            )
                        )
                        feedingButton.visibility = View.GONE
                        feedingInfo.visibility = View.VISIBLE
                    } else {
                        alertMessage("Failed to feed pet. Please try again.", petContainer)
                    }
                }
            }
        }

        // Add the new pet layout to the container
        petContainer.addView(petLayout)
    }


    // Check if NetworkManager is initialized
    private fun checkNetworkManager() {
        if (NetworkManager.isInitialized()) {
            mainRepository = NetworkManager.mainRepository
            apiService = NetworkManager.apiService
            Log.d(TAG, "NetworkManager is initialized.")
            // You can now use mainRepository and apiService
        } else {
            Log.e(TAG, "NetworkManager is not initialized.")
            throw IllegalStateException("NetworkManager is not initialized.")
        }
    }

    private fun alertMessage(message: String, container: LinearLayout) {
        val warning = AlertDialog.Builder(this)
        warning.setTitle("Error")
        warning.setMessage(message)
        warning.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        warning.show()
    }


}
