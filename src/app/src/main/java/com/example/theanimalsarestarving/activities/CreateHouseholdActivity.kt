package com.example.theanimalsarestarving.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.models.User

class CreateHouseholdActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CreateHouseholdActivity"
    }

    private lateinit var userInputHouseholdName: EditText
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_household_activity)

        userInputHouseholdName = findViewById(R.id.user_input_household_name)
        createButton = findViewById(R.id.create_button)

        // Set up the button click listener
        createButton.setOnClickListener {
            // Get the name entered by the user
            val householdName = userInputHouseholdName.text.toString().trim()
            Log.d(TAG, "household name entered: $householdName")
            }
        }
    }

