package com.example.theanimalsarestarving.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
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

        feedingButton = findViewById(R.id.restricted_feed_button)
        logoutButton = findViewById(R.id.restricted_logout_button)

        feedingButton.setOnClickListener {
            val intent = Intent(this, FeedingActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // Function to show the confirmation dialog for logging out
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                // Handle the logout logic
                val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()  // Dismiss the dialog if the user presses "No"
            }
        val alert = builder.create()
        alert.show()
    }
}
