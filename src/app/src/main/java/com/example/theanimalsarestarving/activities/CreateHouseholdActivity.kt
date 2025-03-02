package com.example.theanimalsarestarving.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.repositories.HouseholdRepository
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

        userInputHouseholdName = findViewById(R.id.user_input_household_name)
        createButton = findViewById(R.id.create_button)

        createButton.setOnClickListener {
            val householdName = userInputHouseholdName.text.toString().trim()

            // Ensure the household name is not empty
            if (householdName.isNotEmpty()) {
                val requestBody = mapOf(
                    "householdName" to householdName,
                    "managerEmail" to "test2@gmail.com" //TODO: dynamically implement
                )

                // Make the network request using a coroutine
                lifecycleScope.launch {
                    HouseholdRepository.createHousehold(requestBody)
                }
            }
        }
    }
}