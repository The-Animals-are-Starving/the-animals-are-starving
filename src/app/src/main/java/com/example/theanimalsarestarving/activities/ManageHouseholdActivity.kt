package com.example.theanimalsarestarving.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.activities.helper.PetManagementHelper
import com.example.theanimalsarestarving.activities.helper.UserManagementHelper
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.PetRepository

class ManageHouseholdActivity : AppCompatActivity() {

    lateinit var translationHelper: TranslationHelper
    private val currHouseholdId =
        if (HouseholdRepository.getCurrentHousehold() != null) HouseholdRepository.getCurrentHousehold()?._id else 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_activity)

        translationHelper = intent.getSerializableExtra("translationHelperVar") as? TranslationHelper
            ?: TranslationHelper() // default instance if none provided

        val newUserButton = findViewById<Button>(R.id.newUserButton)
        val newPetButton = findViewById<Button>(R.id.newPetButton)

        newUserButton.setOnClickListener {
            UserManagementHelper.showAddUserDialog(this, translationHelper, currHouseholdId)
        }
        newPetButton.setOnClickListener {
            PetManagementHelper.showAddPetDialog(this, translationHelper, currHouseholdId)
        }

        Log.d("ManageHouseholdActivity", "Current Household: $currHouseholdId\n" +
                "Current User: ${CurrUserRepository.getCurrUser()}\n" +
                "Current Pets: ${PetRepository.getPets()}")

        UserManagementHelper.refreshUsers(this, translationHelper, currHouseholdId)
        PetManagementHelper.refreshPets(this, translationHelper, currHouseholdId)
    }
}