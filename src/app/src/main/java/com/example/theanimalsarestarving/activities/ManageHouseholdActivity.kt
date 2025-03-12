package com.example.theanimalsarestarving.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.models.UserRole
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.PetRepository
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import retrofit2.Call
import retrofit2.Response

private val testHouseholdId: String = "67c2aa855a9890c0f183efa4"


class ManageHouseholdActivity : AppCompatActivity() {
    val currHouseholdId = if (HouseholdRepository.getCurrentHousehold() != null) HouseholdRepository.getCurrentHousehold()?._id else 1

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

        Log.d("ManageHouseholdActivity", "Current Household: ${currHouseholdId}\n Current User: ${CurrUserRepository.getCurrUser()}\n Current pets: ${PetRepository.getPets()}")

        refreshUsers()
        refreshPets()
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
        val newUser = User(name = name, email = email, householdId = currHouseholdId.toString())

        val repository = MainRepository(apiService)
        Log.d("AddUser","Attempting to add user: $newUser")
        repository.addUser(newUser) { addedUser -> //adds user
            if (addedUser != null) {

                Log.d("AddUser", "User added successfully: $addedUser")
                refreshUsers()

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
        val newPet = Pet(name = name, feedingTime = time, householdId = currHouseholdId.toString())

        val repository = MainRepository(apiService)
        Log.d("AddPet", "Attempting to add new pet $newPet")
        repository.addPet(newPet) { addedPet ->
            if (addedPet != null) {
                Log.d("AddPet", "Pet added successfully $newPet")
                refreshPets()

            } else {
                alertMessage("Failed to add pet. Please try again.", container)
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
        if (currHouseholdId != null) {
            repository.getAllUsers(currHouseholdId.toString()) { users ->
                if (users != null) {
                    for (user in users) {
                        val userRow = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(10, 10, 10, 10)
                        }

                        val nameView = TextView(this).apply {
                            text = user.name
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }
                        userRow.addView(nameView)

                        if (user.email != CurrUserRepository.getCurrUser()?.email) {
                            val roleSpinner = Spinner(this)
                            val roleOptions = arrayOf("normal", "restricted", "manager")
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roleOptions)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Set dropdown item style
                            roleSpinner.adapter = adapter

                        // Set current role
                        roleSpinner.setSelection(roleOptions.indexOf(user.role))

                        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                val selectedRole = roleOptions[position]

                                // Prevent unnecessary API calls if role is unchanged
                                val role = user.role
                                if (selectedRole != role) {
                                    roleSpinner.isEnabled = false
                                    updateUserRole(user.email, selectedRole, roleSpinner)
                                }
                            }

                                override fun onNothingSelected(parent: AdapterView<*>) {}
                            }

                            val deleteButton = Button(this).apply {
                                text = "Delete"
                                setOnClickListener {
                                    AlertDialog.Builder(this@ManageHouseholdActivity)
                                        .setTitle("Confirm Deletion")
                                        .setMessage("Are you sure you want to delete this user?")
                                        .setPositiveButton("YES") { _, _ ->
                                            deleteUser(user.email) { success ->
                                                if (success) {
                                                    Toast.makeText(
                                                        this@ManageHouseholdActivity,
                                                        "User Deleted",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    refreshUsers()
                                                } else {
                                                    alertMessage(
                                                        "Failed to delete user. Please try again.",
                                                        userListContainer
                                                    )
                                                }
                                            }
                                        }
                                        .setNegativeButton("NO") { dialog, _ -> dialog.cancel() }
                                        .show()
                                }
                            }
                            userRow.addView(roleSpinner)
                            userRow.addView(deleteButton)
                        } else {
                            val loggedInBox = TextView(this).apply {
                                text = "LOGGED IN"
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(0, 0, 10, 0)
                                }
                                setPadding(20, 15, 20, 15)
                                setBackgroundColor(Color.parseColor("#ADD8E6"))
                                gravity = Gravity.CENTER
                                textSize = 16f
                                setTypeface(null, Typeface.BOLD)
                            }
                            userRow.addView(loggedInBox)
                        }

                        userListContainer.addView(userRow)

                    }
                } else {
                    alertMessage("Failed to fetch users. Please try again.", userListContainer)
                }
            }
        }
    }

    private fun refreshPets() {

        val petListContainer = findViewById<LinearLayout>(R.id.petListContainer)
        petListContainer.removeAllViews() 

        val repository = MainRepository(apiService)
        if (currHouseholdId != null) {
            repository.getAllPets(currHouseholdId.toString()) { pets ->
                if (pets != null) {
                    for (pet in pets) {
                        val petRow = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(10, 10, 10, 10)
                        }

                        val petNameView = TextView(this).apply {
                            text = pet.name
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }

                        val editButton = Button(this).apply {
                            text = "Edit"
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setOnClickListener { showEditPopup(petNameView) }
                        }

                        petRow.addView(petNameView)
                        petRow.addView(editButton)
                        petListContainer.addView(petRow)
                        //                        Log.d(TAG, "Pet added successfully: $addedPet") //TODO: Broken Log
                    }
                } else {
                    alertMessage("Failed to fetch pets. Please try again.", petListContainer)
                }
            }
        }
    }

    private fun updateUserRole(userId: String, newRole: String, spinner: Spinner) {
        val repository = MainRepository(apiService)
        Log.d("UpdateUserRole", "Updating user role for user: $userId to role: $newRole")

        // Disable the spinner selection to prevent multiple selections while the request is in progress
        spinner.isEnabled = false

        // Call the backend to update the user role
        repository.updateUserRole(userId, newRole) { success ->

            if (success) {
                Log.d("UpdateUserRole", "User role updated successfully")
            } else {
                alertMessage("Failed to update role. Try again.", spinner.parent as LinearLayout)
                // Reset the spinner to its previous role (in case of failure)
                val roleOptions = arrayOf("normal", "restricted", "manager")
                val adapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, roleOptions)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                val selectedPosition = roleOptions.indexOf(newRole)
                if (selectedPosition >= 0) {
                    spinner.setSelection(selectedPosition)
                }
            }

            spinner.isEnabled = true

        }
    }

    private fun deleteUser(userEmail: String, callback: (Boolean) -> Unit) {
        Log.d("ManageHousehold", "Attempting to delete user $userEmail")
        apiService.deleteUser(userEmail).enqueue(object : retrofit2.Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if(response.isSuccessful) {
                    val success = response.body() ?: false
                    if (success) {
                        Log.d("DeleteUser", "User deleted successfully")
                    } else {
                        Log.d("DeleteUser", "User not found or already deleted")
                    }
                } else {
                    Log.e("DeleteUser", "Failed with code: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.e("DeleteUser", "Error: ${t.message}")
            }
        })
        callback(true)
        //TODO: Make backend call
    }


}
