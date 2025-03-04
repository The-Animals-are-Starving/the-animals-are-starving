package com.example.theanimalsarestarving.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.UserRole
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.PetRepository
import com.example.theanimalsarestarving.repositories.UserRepository
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
        Log.d(TAG, "Creating household! Current Household: ${HouseholdRepository.getCurrentHousehold()}\n Current User: ${CurrUserRepository.getCurrUser()}\n Current pets: ${PetRepository.getPets()}")

        userInputHouseholdName = findViewById(R.id.user_input_household_name)
        createButton = findViewById(R.id.create_button)

        createButton.setOnClickListener {
            lifecycleScope.launch {
                val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
//                val managerEmail = sharedPreferences.getString("userEmail", "").toString()
                val managerEmail = "test1234@gmail.com"
//                val managerName = sharedPreferences.getString("userName", "").toString()
                val managerName = "tj"
                val householdName = userInputHouseholdName.text.toString().trim()
                if(householdName.isNotEmpty()) {
                    try {
                        UserRepository.createUser(managerEmail, managerName, "") //null string or ""?

                        Log.d(TAG, "creating household with manager email: $managerEmail")
                        // Make sure the household creation completes before proceeding
                        val householdCreated = HouseholdRepository.createHousehold(householdName, managerEmail) // Ensure this is a suspend function

                        if (householdCreated!= null) {

                            HouseholdRepository.setCurrentHousehold(householdCreated) //sets current household singleton
                            //TODO: Update user to have householdid
//                            UserRepository.addUserToHousehold(householdCreated._id, managerEmail)  //when you create the household, the current user is automatically added
                            UserRepository.updateUserRole(managerEmail, UserRole.ADMIN)


                            // Move to the MainActivity after both tasks are done
                            val intent = Intent(this@CreateHouseholdActivity, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.e(TAG, "Household creation failed")
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error during household creation or user updates: ${e.message}")
                    }
                }
            }
        }
    }
}
