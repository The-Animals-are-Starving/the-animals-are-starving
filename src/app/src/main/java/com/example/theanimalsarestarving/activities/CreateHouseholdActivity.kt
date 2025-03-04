package com.example.theanimalsarestarving.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.models.UserRole
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.repositories.PetRepository
import com.example.theanimalsarestarving.repositories.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CreateHouseholdActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CreateHouseholdActivity"
    }

    private lateinit var userInputHouseholdName: EditText
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_household_activity)
        Log.d(TAG, "onCreate")
        Log.d(TAG, "Creating household! Current Household: ${HouseholdRepository.getCurrentHousehold()}\n Current User: ${CurrUserRepository.getCurrUser()}\n Current pets: ${PetRepository.getPets()}")

        userInputHouseholdName = findViewById(R.id.user_input_household_name)
        createButton = findViewById(R.id.create_button)

        createButton.setOnClickListener {
            lifecycleScope.launch {
                val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                val managerEmail = sharedPreferences.getString("userEmail", "").toString()
                val managerName = sharedPreferences.getString("userName", "").toString()
                val householdName = userInputHouseholdName.text.toString().trim()

                if (householdName.isNotEmpty()) {
                    try {
                        Log.d(TAG, "creating household with manager email: $managerEmail")
                        createHousehold(householdName, managerEmail)
                        addUser(managerName, managerEmail)

                        // Move to the MainActivity
                        val intent = Intent(this@CreateHouseholdActivity, MainActivity::class.java)
                        startActivity(intent)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error during household creation or user update: ${e.message}")
                    }
                }
            }
        }
    }
    private fun createHousehold(householdName: String, managerEmail: String) {
        val requestBody = mapOf(
            "householdName" to householdName,
            "managerEmail" to managerEmail
        )

        lifecycleScope.launch {
            try {
                HouseholdRepository.createHousehold(requestBody)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating household: ${e.message}")
            }
        }
    }

    private fun setCurrentHousehold() {
        val currentHousehold = HouseholdRepository.getCurrentHousehold()

        if (currentHousehold != null) {
            // Use the setter to set the current household in the repository
            HouseholdRepository.setCurrentHousehold(currentHousehold)
            Log.d(TAG, "Successfully set current household: $currentHousehold")
        } else {
            Log.e(TAG, "Failed to set current household.")
        }
    }

    private fun addUser(name: String, email: String) {
        Log.d(TAG, "HAHAHHAHAHHA? ")

        val newUser = User(name = name, email = email, householdId = "HOUSE")


        Log.d(TAG, "HAHAHHAHAHHA: " + newUser)

        val repository = MainRepository(apiService)
        Log.d("AddUser","Attempting to add user: $newUser")

        repository.addUser(newUser) { addedUser -> //adds user
            if (addedUser != null) {

                Log.d("AddUser", "User added successfully: $addedUser")

            } else {
                Log.d("AddUser", "Failed to add user")
            }
        }
    }
}
