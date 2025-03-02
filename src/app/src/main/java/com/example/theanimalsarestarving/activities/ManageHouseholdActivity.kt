package com.example.theanimalsarestarving.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.PetRepository
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
private val testHouseholdId: String = "67c2aa855a9890c0f183efa4"


class ManageHouseholdActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ManageHouseholdActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_activity)

        val userListContainer = findViewById<LinearLayout>(R.id.userListContainer)
        val newUserButton = findViewById<Button>(R.id.newUserButton)

        val petListContainer = findViewById<LinearLayout>(R.id.petListContainer)
        val newPetButton = findViewById<Button>(R.id.newPetButton)

        newUserButton.setOnClickListener {
            showAddUserDialog(userListContainer)
        }
        newPetButton.setOnClickListener {
            showAddPetDialog(petListContainer)
        }

        refreshUsers()
    }

    private fun showAddUserDialog(container: LinearLayout) { //Popup for new user
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New User")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val nameIn = EditText(this).apply {
            hint = "Enter new user"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        layout.addView(nameIn)

        val emailIn = EditText(this).apply {
            hint = "Enter user email"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        layout.addView(emailIn)
        builder.setView(layout)

        // Submit
        builder.setPositiveButton("Add") { _, _ ->
            val userName = nameIn.text.toString().trim().replace("\n", "")
            val email = emailIn.text.toString().trim().replace("\n", "")


            if (!isValidEmail(email)) {
                alertMessage("Please Enter a Valid Email", container)
            } else if (userName.isNotEmpty() && email.isNotEmpty()) {
                addUser(userName, email, container)
            } else {
                alertMessage("Please Enter all User Info", container)
            }
        }

        // Set negative button (cancel)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }


        builder.show()
    }

    private fun addUser(name: String, email: String, container: LinearLayout) {
        val newUser = User(name = name, email = email, householdId = HouseholdRepository.getCurrentHousehold()?._id.toString())

        val repository = MainRepository(apiService)
        Log.d("AddUser","Attempting to add user: $newUser")
        repository.addUser(newUser) { addedUser -> //adds user
            if (addedUser != null) {
                val switch = SwitchCompat(this).apply {
                    text = name
                    isChecked = false
                }
                container.addView(switch)
                Log.d("AddUser", "User added successfully: $addedUser")

                Log.d("AddUserToHousehold","Attempting to add user to household: $newUser")
                repository.addUserToHousehold(newUser, newUser.householdId) { includedUser -> //adds user
                    if (includedUser != null) {
                        Log.d("AddUserToHousehold", "User added to household successfully: $includedUser")
                    } else {
                        alertMessage("Failed to add user to household. Please try again.", container)
                    }
                }

            } else {
                alertMessage("Failed to add user. Please try again.", container)
            }
        }
    }


    private fun showAddPetDialog(container: LinearLayout) { //Popup for new pet
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Pet")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }


        val nameIn = EditText(this).apply {
            hint = "Enter pet name"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        layout.addView(nameIn)

        val feedingTime = EditText(this).apply {
            hint = "Feeding Time"
            isFocusable = false
            setOnClickListener{ showTimePicker(this) }
        }
        layout.addView(feedingTime)

        val typeIn = Spinner(this)
        val petTypeOptions = arrayOf("Select Pet Type","Dog", "Cat", "Rabbit", "Hamster", "Fish", "Lizard", "Bird", "Other")
        typeIn.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, petTypeOptions)

        layout.addView(typeIn)

        builder.setView(layout)
        // Submit
        builder.setPositiveButton("Add") { _, _ ->
            val petName = nameIn.text.toString().trim().replace("\n", "")
            val petType = typeIn.selectedItem.toString()
            val petFeedTime = feedingTime.text.toString()
            if (petName.isNotEmpty() || petType != "Select Pet Type") {
                addPet(petName, petType, petFeedTime ,container)
            } else {
                alertMessage("Please Enter all Pet Info", container)
            }
        }

        // Set negative button (cancel)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }


        builder.show()
    }

    /**
     * Shows addPet popup for entering pet data
     */
    private fun addPet(name: String, type: String, time: String, container: LinearLayout) {
        lifecycleScope.launch {
            // Construct the request body
            val requestBody = mapOf(
                "name" to name,
                "householdId" to HouseholdRepository.getCurrentHousehold()?._id.toString(),
                "feedingTime" to time
            )

            try {
                // Make the network request to add the pet
                val addedPet = PetRepository.addPetToHousehold(requestBody)

                if (addedPet != null) {
                    // Now that the pet is added, update the UI (on the main thread)
                    withContext(Dispatchers.Main) {
                        val petRow = LinearLayout(this@ManageHouseholdActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(10, 10, 10, 10)
                        }

                        val petNameView = TextView(this@ManageHouseholdActivity).apply {
                            text = name
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }

                        val editButton = Button(this@ManageHouseholdActivity).apply {
                            text = "Edit"
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setOnClickListener { showEditPopup(petNameView) }
                        }

                        petRow.addView(petNameView)
                        petRow.addView(editButton)
                        container.addView(petRow)
                        Log.d("AddPet", "Pet added successfully: $addedPet")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        alertMessage("Failed to add pet. Please try again", container)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding pet: ${e.message}")
                withContext(Dispatchers.Main) {
                    alertMessage("Error adding pet. Please try again", container)
                }
            }
        }
    }

    /**
     * Shows clock popup for selecting feeding time
     */
    @SuppressLint("DefaultLocale")
    private fun showTimePicker(editText: EditText) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Feeding Time")
            .build()

        timePicker.show(supportFragmentManager, "time_picker")

        timePicker.addOnPositiveButtonClickListener{
            val formattedTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            editText.setText(formattedTime)
        }
    }

    private fun showEditPopup(petTextView: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Pet")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val editName = EditText(this).apply { //TODO: Change this to edit pet params
            hint = "Pet Name"
            setText(petTextView.text)
            setSingleLine(true)
        }
        layout.addView(editName)

        val editTime = EditText(this).apply {
            hint = "Change Feeding Time"
            isFocusable = false
            setOnClickListener{ showTimePicker(this) }
        }
        layout.addView(editTime)
        builder.setView(layout)

        builder.setPositiveButton("Save"){_, _ ->
            petTextView.text = editName.text.toString()
            //TODO: Change this to send to backend
        }

        builder.show()
    }

    private fun petExists(name: String, container: LinearLayout): Boolean {
        //TODO: Check pet list to see if pet already exists in backend
        return false
    }

    private fun alertMessage(message: String, container: LinearLayout) {
        val warning = AlertDialog.Builder(this)
        warning.setTitle("Error")
        warning.setMessage(message)
        warning.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        warning.show()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun refreshUsers() {
        val userListContainer = findViewById<LinearLayout>(R.id.userListContainer)
        userListContainer.removeAllViews() // Clear existing user list

        val repository = MainRepository(apiService)
        repository.getAllUsers(testHouseholdId) { users ->
            if (users != null) {
                for (user in users) {
                    val switch = SwitchCompat(this).apply {
                        text = user.name
                        isChecked = false
                    }
                    userListContainer.addView(switch)
                }
            } else {
                alertMessage("Failed to fetch users. Please try again.", userListContainer)
            }
        }
    }
}
