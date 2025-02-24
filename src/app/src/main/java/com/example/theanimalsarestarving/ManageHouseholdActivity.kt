package com.example.theanimalsarestarving

import android.app.AlertDialog
import android.os.Bundle
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

        val dialog = builder.create()

        val input = EditText(this).apply {
            hint = "Enter new user"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        builder.setView(input)

        // Submit
        builder.setPositiveButton("Add") { _, _ ->
            val userName = input.text.toString().trim().replace("\n", "")
            if (userName.isNotEmpty()) {
                addUser(userName, container)
            }
        }

        // Set negative button (cancel)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }


        builder.show()
    }

    private fun addUser(name: String, container: LinearLayout) {
        if (!userExists(name, container)) {
            val switch = SwitchCompat(this).apply {
                text = name
                isChecked = false
            }
            container.addView(switch)

            //TODO: Also send user to backend
        } else {
            AlertDialog.Builder(this)
                .setMessage("User Already Exists!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
        }
    }

    private fun userExists(name: String, container: LinearLayout): Boolean {
        //TODO: Check user list to see if user already exists
        return false
    }


    private fun showAddPetDialog(container: LinearLayout) { //Popup for new pet
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Pet")

        val dialog = builder.create()

        val input = EditText(this).apply {
            hint = "Enter new pet"
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
        builder.setView(input)

        // Submit
        builder.setPositiveButton("Add") { _, _ ->
            val petName = input.text.toString().trim().replace("\n", "")
            if (petName.isNotEmpty()) {
                addPet(petName, container)
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
            AlertDialog.Builder(this)
                .setMessage("Pet Already Exists!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
        }
    }

    private fun petExists(name: String, container: LinearLayout): Boolean {
        //TODO: Check pet list to see if pet already exists
        return false
    }
}
