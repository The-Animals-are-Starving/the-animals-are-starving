package com.example.theanimalsarestarving.activities.helper

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.activities.ManageHouseholdActivity
import com.example.theanimalsarestarving.activities.TranslationHelper
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.network.NetworkManager
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.utils.AppUtils

object UserManagementHelper {

    fun showAddUserDialog(activity: ManageHouseholdActivity, translationHelper: TranslationHelper, householdId: Any?) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Add New User")

        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val nameIn = EditText(activity).apply {
            hint = "Enter new user"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        layout.addView(nameIn)

        val emailIn = EditText(activity).apply {
            hint = "Enter user email"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        layout.addView(emailIn)
        builder.setView(layout)

        builder.setPositiveButton("Add") { _, _ ->
            val userName = nameIn.text.toString().trim().replace("\n", "")
            val email = emailIn.text.toString().trim().replace("\n", "")
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                AppUtils.alertMessage(activity, "Please Enter a Valid Email")
            } else if (userName.isNotEmpty() && email.isNotEmpty()) {
                addUser(activity, userName, email, householdId)
            } else {
                AppUtils.alertMessage(activity, "Please Enter all User Info")
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
        translationHelper.updateLanguageUI(translationHelper, activity.findViewById(R.id.manage_household_activity), activity.lifecycleScope)
    }

    private fun addUser(activity: ManageHouseholdActivity, name: String, email: String, householdId: Any?) {
        val newUser = User(name = name, email = email, householdId = householdId.toString())
        val repository = MainRepository(NetworkManager.apiService, NetworkManager.userApiService)
        Log.d("AddUser", "Attempting to add user: $newUser")
        repository.addUser(newUser) { addedUser ->
            if (addedUser != null) {
                Log.d("AddUser", "User added successfully: $addedUser")
                refreshUsers(activity, TranslationHelper(), householdId)
            } else {
                AppUtils.alertMessage(activity, "Failed to add user. Please try again.")
            }
        }
    }

    fun refreshUsers(activity: ManageHouseholdActivity, translationHelper: TranslationHelper, householdId: Any?) {
        val userListContainer = activity.findViewById<LinearLayout>(R.id.userListContainer)
        userListContainer.removeAllViews()

        val repository = MainRepository(NetworkManager.apiService, NetworkManager.userApiService)
        if (householdId != null) {
            repository.getAllUsers(householdId.toString()) { users ->
                if (users != null) {
                    for (user in users) {
                        val userRow = LinearLayout(activity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(10, 10, 10, 10)
                        }

                        val nameView = createNameView(activity, user)
                        userRow.addView(nameView)

                        if (user.email != CurrUserRepository.getCurrUser()?.email) {
                            val roleSpinner = createRoleSpinner(activity, user)
                            userRow.addView(roleSpinner)
                            val deleteButton = createDeleteButton(activity, user)
                            userRow.addView(deleteButton)
                        } else {
                            val loggedInBox = TextView(activity).apply {
                                text = activity.getString(R.string.logged_in_text)
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply { setMargins(0, 0, 10, 0) }
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
                    translationHelper.updateLanguageUI(translationHelper, activity.findViewById(R.id.manage_household_activity), activity.lifecycleScope)
                } else {
                    AppUtils.alertMessage(activity, "Failed to fetch users. Please try again.")
                }
            }
        }
    }

    private fun createNameView(activity: ManageHouseholdActivity, user: User): TextView {
        return TextView(activity).apply {
            text = user.name
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
    }

    private fun createDeleteButton(activity: ManageHouseholdActivity, user: User): Button {
        return Button(activity).apply {
            text = activity.getString(R.string.delete_text)
            setOnClickListener {
                AlertDialog.Builder(activity)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("YES") { _, _ ->
                        deleteUser(activity, user.email) { success ->
                            if (success) {
                                Toast.makeText(activity, "User Deleted", Toast.LENGTH_SHORT).show()
                                refreshUsers(activity, TranslationHelper(), user.householdId)
                            } else {
                                AppUtils.alertMessage(activity, "Failed to delete user. Please try again.")
                            }
                        }
                    }
                    .setNegativeButton("NO") { dialog, _ -> dialog.cancel() }
                    .show()
            }
        }
    }

    private fun createRoleSpinner(activity: ManageHouseholdActivity, user: User): Spinner {
        val roleSpinner = Spinner(activity)
        val roleOptions = arrayOf("normal", "restricted", "manager")
        val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, roleOptions)
        roleSpinner.adapter = adapter
        roleSpinner.setSelection(roleOptions.indexOf(user.role))
        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRole = roleOptions[position]
                if (selectedRole != user.role) {
                    roleSpinner.isEnabled = false
                    updateUserRole(activity, user.email, selectedRole, roleSpinner)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        return roleSpinner
    }

    private fun updateUserRole(activity: ManageHouseholdActivity, userId: String, newRole: String, spinner: Spinner) {
        val repository = MainRepository(NetworkManager.apiService, NetworkManager.userApiService)
        Log.d("UpdateUserRole", "Updating user role for user: $userId to role: $newRole")
        spinner.isEnabled = false
        repository.updateUserRole(userId, newRole) { success ->
            if (success) {
                Log.d("UpdateUserRole", "User role updated successfully")
            } else {
                AppUtils.alertMessage(activity, "Failed to update role. Try again.")
                val roleOptions = arrayOf("normal", "restricted", "manager")
                val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, roleOptions)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                spinner.setSelection(roleOptions.indexOf(newRole))
            }
            spinner.isEnabled = true
        }
    }

    private fun deleteUser(activity: ManageHouseholdActivity, userEmail: String, callback: (Boolean) -> Unit) {
        Log.d("ManageHousehold", "Attempting to delete user $userEmail")
        NetworkManager.userApiService.deleteUser(userEmail).enqueue(object : retrofit2.Callback<Boolean> {
            override fun onResponse(call: retrofit2.Call<Boolean>, response: retrofit2.Response<Boolean>) {
                if (response.isSuccessful) {
                    val success = response.body() ?: false
                    if (success) Log.d("DeleteUser", "User deleted successfully")
                    else Log.d("DeleteUser", "User not found or already deleted")
                    callback(success)
                } else {
                    Log.e("DeleteUser", "Failed with code: ${response.code()}")
                    callback(false)
                }
            }
            override fun onFailure(call: retrofit2.Call<Boolean>, t: Throwable) {
                Log.e("DeleteUser", "Error: ${t.message}")
                callback(false)
            }
        })
    }
}
