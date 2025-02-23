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

        newUserButton.setOnClickListener {
            showAddUserDialog(userListContainer)
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
}
