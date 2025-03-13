package com.example.theanimalsarestarving.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.theanimalsarestarving.R

class RestrictedMainActivity : AppCompatActivity() {

    private val TAG = "RestrictedMainActivity"
    private lateinit var feedingButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_restricted)

        Log.d(TAG, "In Restricted Main Activity")
        setupButtons()

        Log.d(TAG, "onCreate: Buttons are set up successfully")
    }

    // Helper function to set up buttons
    private fun setupButtons() {
        // Log the button setup
        Log.d(TAG, "setupButtons: Initializing buttons")

        feedingButton = findViewById(R.id.restricted_feed_button)
        logoutButton = findViewById(R.id.restricted_logout_button)

        Log.d(TAG, "setupButtons: Buttons initialized, setting listeners")

        // Setup feeding button listener
        feedingButton.setOnClickListener {
            Log.d(TAG, "feedingButton: Clicked, navigating to FeedingActivity")
            val intent = Intent(this, FeedingActivity::class.java)
            startActivity(intent)
        }

        // Setup logout button listener
        logoutButton.setOnClickListener {
            Log.d(TAG, "logoutButton: Clicked, showing logout confirmation")
            showLogoutConfirmationDialog()
        }
    }

    // Function to show the confirmation dialog for logging out
    private fun showLogoutConfirmationDialog() {
        Log.d(TAG, "showLogoutConfirmationDialog: Showing confirmation dialog")

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                Log.d(TAG, "showLogoutConfirmationDialog: User confirmed logout")
                // Handle the logout logic
                val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, id ->
                Log.d(TAG, "showLogoutConfirmationDialog: User cancelled logout")
                dialog.dismiss()  // Dismiss the dialog if the user presses "No"
            }
        val alert = builder.create()
        alert.show()
    }
}
