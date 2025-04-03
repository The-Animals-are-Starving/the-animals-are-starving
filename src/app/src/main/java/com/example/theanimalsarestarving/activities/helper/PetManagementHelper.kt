package com.example.theanimalsarestarving.activities.helper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.activities.ManageHouseholdActivity
import com.example.theanimalsarestarving.activities.TranslationHelper
import com.example.theanimalsarestarving.models.Pet
import com.example.theanimalsarestarving.network.NetworkManager
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.utils.AppUtils
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

object PetManagementHelper {

    fun showAddPetDialog(activity: ManageHouseholdActivity, translationHelper: TranslationHelper, householdId: Any?) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Add New Pet")
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val nameIn = EditText(activity).apply {
            hint = "Enter pet name"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        layout.addView(nameIn)

        val feedingTime = EditText(activity).apply {
            hint = "Feeding Time"
            isFocusable = false
            setOnClickListener { showTimePicker(activity, this) }
        }
        layout.addView(feedingTime)

        val typeIn = Spinner(activity)
        val petTypeOptions = arrayOf("Select Pet Type", "Dog", "Cat", "Rabbit", "Hamster", "Fish", "Lizard", "Bird", "Other")
        typeIn.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, petTypeOptions)
        layout.addView(typeIn)

        builder.setView(layout)
        builder.setPositiveButton("Add") { _, _ ->
            val petName = nameIn.text.toString().trim().replace("\n", "")
            val petFeedTime = feedingTime.text.toString()
            if (petName.isNotEmpty()) {
                addPet(activity, petName, petFeedTime, householdId)
            } else {
                AppUtils.alertMessage(activity, "Please Enter all Pet Info")
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun addPet(activity: ManageHouseholdActivity, name: String, time: String, householdId: Any?) {
        val newPet = Pet(name = name, feedingTime = time, householdId = householdId.toString())
        val repository = MainRepository(NetworkManager.apiService, NetworkManager.userApiService)
        Log.d("AddPet", "Attempting to add new pet $newPet")
        repository.addPet(newPet) { addedPet ->
            if (addedPet != null) {
                Log.d("AddPet", "Pet added successfully $newPet")
                refreshPets(activity, TranslationHelper(), householdId)
            } else {
                AppUtils.alertMessage(activity, "Failed to add pet. Please try again.")
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePicker(activity: ManageHouseholdActivity, editText: EditText) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Feeding Time")
            .build()

        timePicker.show(activity.supportFragmentManager, "time_picker")
        timePicker.addOnPositiveButtonClickListener {
            val formattedTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            editText.setText(formattedTime)
        }
    }

    fun refreshPets(activity: ManageHouseholdActivity, translationHelper: TranslationHelper, householdId: Any?) {
        val petListContainer = activity.findViewById<LinearLayout>(R.id.petListContainer)
        petListContainer.removeAllViews()
        val repository = MainRepository(NetworkManager.apiService, NetworkManager.userApiService)
        if (householdId != null) {
            repository.getAllPets(householdId.toString()) { pets ->
                if (pets != null) {
                    for (pet in pets) {
                        val petRow = LinearLayout(activity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(10, 10, 10, 10)
                        }
                        val petNameView = TextView(activity).apply {
                            text = pet.name
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        }
                        val deleteButton = createDeleteButton(activity, pet)
                        petRow.addView(petNameView)
                        petRow.addView(deleteButton)
                        petListContainer.addView(petRow)
                    }
                    translationHelper.updateLanguageUI(translationHelper, activity.findViewById(R.id.manage_household_activity), activity.lifecycleScope)
                } else {
                    AppUtils.alertMessage(activity, "Failed to fetch pets. Please try again.")
                }
            }
        }
    }

    private fun createDeleteButton(activity: ManageHouseholdActivity, pet: Pet): Button {
        return Button(activity).apply {
            text = activity.getString(R.string.delete_text)
            setOnClickListener {
                AlertDialog.Builder(activity)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this pet?")
                    .setPositiveButton("YES") { _, _ ->
                        deletePet(activity, pet.name) { success ->
                            if (success) {
                                Toast.makeText(activity, "Pet Deleted", Toast.LENGTH_SHORT).show()
                                refreshPets(activity, TranslationHelper(), pet.householdId)
                            } else {
                                AppUtils.alertMessage(activity, "Failed to delete Pet. Please try again.")
                            }
                        }
                    }
                    .setNegativeButton("NO") { dialog, _ -> dialog.cancel() }
                    .show()
            }
        }
    }

    private fun deletePet(activity: ManageHouseholdActivity, petName: String, callback: (Boolean) -> Unit) {
        Log.d("ManageHousehold", "Attempting to delete pet $petName")
        NetworkManager.apiService.deletePet(petName).enqueue(object : retrofit2.Callback<Boolean> {
            override fun onResponse(call: retrofit2.Call<Boolean>, response: retrofit2.Response<Boolean>) {
                if (response.isSuccessful) {
                    val success = response.body() ?: false
                    if (success) Log.d("DeletePet", "Pet deleted successfully") else Log.d("DeletePet", "Pet not found or already deleted")
                    callback(success)
                } else {
                    Log.e("DeletePet", "Failed with code: ${response.code()}")
                    callback(false)
                }
            }
            override fun onFailure(call: retrofit2.Call<Boolean>, t: Throwable) {
                Log.e("DeletePet", "Error: ${t.message}")
                callback(false)
            }
        })
    }
}