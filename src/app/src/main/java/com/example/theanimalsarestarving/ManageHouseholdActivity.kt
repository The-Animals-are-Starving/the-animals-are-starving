package com.example.theanimalsarestarving

import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.contains
import java.security.Key

class ManageHouseholdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

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
        if (!userExists(name, container)) {
            val switch = SwitchCompat(this).apply {
                text = name
                isChecked = false
            }
            container.addView(switch)

            //TODO: Also send user to backend
        } else {
            alertMessage("The User Already Exists!", container)
        }
    }

    private fun userExists(name: String, container: LinearLayout): Boolean {
        //TODO: Check user list to see if user already exists
        return false
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

        val typeIn = EditText(this).apply {
            hint = "Enter pet type (dog, cat, etc.)"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        layout.addView(typeIn)

        builder.setView(layout)
        // Submit
        builder.setPositiveButton("Add") { _, _ ->
            val petName = nameIn.text.toString().trim().replace("\n", "")
            val petType = typeIn.text.toString().trim().replace("\n", "")
            if (petName.isNotEmpty() || petType.isNotEmpty()) {
                addPet(petName, container)
            } else {
                alertMessage("Please Enter all Pet Info", container)
            }
        }

        // Set negative button (cancel)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }


        builder.show()
    }

    private fun addPet(name: String, container: LinearLayout) {
        if (!petExists(name, container)) {
            //TODO: modify this for the needs of a pet
            val switch = SwitchCompat(this).apply {
                text = name
                isChecked = false
            }
            container.addView(switch)

            //TODO: Also send pet to backend
        } else {
            alertMessage("Pet Already Exists!", container)
        }
    }

    private fun petExists(name: String, container: LinearLayout): Boolean {
        //TODO: Check pet list to see if pet already exists
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
}
