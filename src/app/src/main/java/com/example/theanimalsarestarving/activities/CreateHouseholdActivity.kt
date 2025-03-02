package com.example.theanimalsarestarving.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.repositories.PetRepository
import kotlinx.coroutines.launch

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
        Log.d(TAG, "Current Household: ${HouseholdRepository.getCurrentHousehold()}\n Current User: ${CurrUserRepository.getCurrUser()}\n Current pets: ${PetRepository.getPets()}")

        userInputHouseholdName = findViewById(R.id.user_input_household_name)
        createButton = findViewById(R.id.create_button)

        createButton.setOnClickListener {
            val householdName = userInputHouseholdName.text.toString().trim()

            // Ensure the household name is not empty
            if (householdName.isNotEmpty()) {
                createHousehold(householdName)

                //Redirect to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun createHousehold(householdName: String) {
        val requestBody = mapOf(
            "householdName" to householdName,
            "managerEmail" to CurrUserRepository.getCurrUser()?.email.toString()
        )

        // Make the network request using a coroutine
        lifecycleScope.launch {
            try {
                // Call the repository function to create the household
                HouseholdRepository.createHousehold(requestBody)

                // After creating the household, set the current household
                setCurrentHousehold()
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
}
