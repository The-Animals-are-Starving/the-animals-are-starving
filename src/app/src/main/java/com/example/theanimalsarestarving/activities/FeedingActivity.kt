package com.example.theanimalsarestarving.activities

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.UserRole
import com.example.theanimalsarestarving.network.ApiService
import com.example.theanimalsarestarving.network.NetworkManager
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.LanguageRepository
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.repositories.PetRepository
import com.example.theanimalsarestarving.utils.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class FeedingActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FeedingActivity"
    }

    private lateinit var petContainer: LinearLayout
    private lateinit var mainRepository: MainRepository
    private lateinit var apiService: ApiService
    lateinit var translationHelper: TranslationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feeding_activity)
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            translationHelper = intent.getSerializableExtra("translationHelperVar") as TranslationHelper
        }


        petContainer = findViewById(R.id.petContainer)

        val currUser = CurrUserRepository.getCurrUser()

        if (currUser != null) {
            if (UserRole.fromBackendRole(currUser.role) == UserRole.RESTRICTED) {
                val defaultTitle = findViewById<TextView>(R.id.title)
                val restrictedTitle = findViewById<TextView>(R.id.title_restricted)
                restrictedTitle.visibility = View.VISIBLE
                defaultTitle.visibility = View.GONE
            }
        }
        checkNetworkManager()

        Log.d(TAG, "Current Household: ${HouseholdRepository.getCurrentHousehold()}\n Current User: ${CurrUserRepository.getCurrUser()}\n Current pets: ${PetRepository.getPets()}")

        loadPets(HouseholdRepository.getCurrentHousehold()?._id.toString())

        Log.d(TAG, "Current Language: ${LanguageRepository.language}")
    }


    private fun loadPets(testHouseholdId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                PetRepository.fetchPetsFromDB(testHouseholdId)
                Log.d(TAG, "Pets Fetched: ${PetRepository.currPets}")

                val petImage = R.drawable.dog_default_icon

                for (pet in PetRepository.currPets) {
                    Log.d(TAG, "Pet: $pet")
                    loadPet(pet.name, pet.feedingTime, petImage, pet.fed)
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP Error: ${e.code()} - ${e.message()}")
            } catch (e: IOException) {
                Log.e(TAG, "IOException fetching pets: ${e.message}")
            }
        }
    }

    // Method to add a pet to the container dynamically
    private fun loadPet(petName: String, feedingTime: String, petImageResId: Int, isFed: Boolean) {
        Log.d(TAG, "Loading pet: (petName: $petName, feedingTime: $feedingTime, isFed: $isFed)")
        val petLayout = LayoutInflater.from(this).inflate(R.layout.pet_item, petContainer, false)

        // Set the pet image and name.
        petLayout.findViewById<ImageView>(R.id.pet_image).setImageResource(petImageResId)
        petLayout.findViewById<TextView>(R.id.pet_name).text = petName

        val petCircle = petLayout.findViewById<ImageView>(R.id.pet_circle)
        val fedStatusText = petLayout.findViewById<TextView>(R.id.fed_status)
        val feedingButton = petLayout.findViewById<Button>(R.id.indicate_fed_button)
        val feedingInfo = petLayout.findViewById<TextView>(R.id.feeding_info)

        // Update UI based on feeding status.
        if (!isFed) {
            fedStatusText.text = getString(R.string.not_fed_text)
            petCircle.setColorFilter(ContextCompat.getColor(this, R.color.dark_pink))
            feedingButton.visibility = View.VISIBLE
            feedingInfo.visibility = View.GONE
        } else {
            fedStatusText.text = getString(R.string.fed_text)
            petCircle.setColorFilter(ContextCompat.getColor(this, R.color.base_green))
            feedingButton.visibility = View.GONE
            feedingInfo.visibility = View.VISIBLE
        }

        // Set the click listener to handle feeding.
        feedingButton.setOnClickListener {
            showFeedingDialog(petName, petCircle, fedStatusText, feedingButton, feedingInfo)
        }

        petContainer.addView(petLayout)
        translationHelper.updateLanguageUI(translationHelper, findViewById(R.id.feeding_activity), lifecycleScope)
    }

    private fun showFeedingDialog(
        petName: String,
        petCircle: ImageView,
        fedStatusText: TextView,
        feedingButton: Button,
        feedingInfo: TextView
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Feeding Amount")

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setPadding(40, 20, 40, 20)
        }
        builder.setView(input)

        builder.setPositiveButton("Feed") { _, _ ->
            val feedingAmount = input.text.toString()
            if (feedingAmount.isNotEmpty() && feedingAmount.toIntOrNull() != null) {
                val repository = PetRepository
                repository.feedPet(petName) { success ->
                    if (success) {
                        fedStatusText.text = getString(R.string.fed_text)
                        petCircle.setColorFilter(ContextCompat.getColor(this@FeedingActivity, R.color.base_green))
                        feedingButton.visibility = View.GONE
                        feedingInfo.visibility = View.VISIBLE
                    } else {
                        AppUtils.alertMessage(this, "Failed to feed pet. Please try again.")
                    }
                }
                val currUser = CurrUserRepository.getCurrUser()
                Log.d("FeedingActivity", "Attempting to log feeding for pet $petName, from user $currUser")
                repository.logFeed(petName, currUser?.email.toString(), feedingAmount) { success ->
                    val message = if (success) "Feeding logged successfully" else "Failed to log feeding"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a valid feeding amount", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
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
}
