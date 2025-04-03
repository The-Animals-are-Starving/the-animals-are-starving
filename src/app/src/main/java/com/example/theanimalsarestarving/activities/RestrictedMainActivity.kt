package com.example.theanimalsarestarving.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R

class RestrictedMainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RestrictedMainActivity"
    }

    private lateinit var feedingButton: Button
    private lateinit var logoutButton: Button
    lateinit var translationHelper: TranslationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_restricted)
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            translationHelper = intent.getSerializableExtra("translationHelperVar") as TranslationHelper
        }

        Log.d(TAG, "In Restricted Main Activity")
        setupButtons()

        translationHelper.updateLanguageUI(translationHelper, findViewById(R.id.activity_main_restricted), lifecycleScope)

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

    private fun showLogoutConfirmationDialog() {
        Log.d(TAG, "showLogoutConfirmationDialog: Showing confirmation dialog")

        val builder = AlertDialog.Builder(this)

        // Set message text size using SpannableString (if needed)
        val message = SpannableString("Do you want to log out?")
        message.setSpan(RelativeSizeSpan(1.5f), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        builder.setMessage(message)
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

        // Create the dialog
        val alert = builder.create()

        // Customize the buttons when the dialog is shown
        alert.setOnShowListener {
            // Find the "Yes" button by its ID and make it grey and bigger
            val yesButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            yesButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)  // Set text size to 18sp

            // Find the "No" button by its ID and make it red, bold, and bigger
            val noButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE)
            noButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)  // Set text size to 18sp
            noButton.setTypeface(null, Typeface.BOLD)  // Make the text bold
        }

        // Show the alert dialog
        alert.show()
    }

}
