package com.example.theanimalsarestarving

import UserRoleViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import androidx.lifecycle.Observer


class MainActivity : AppCompatActivity() {


    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")

        val feedingButton: Button = findViewById<Button>(R.id.feed_button)
        val notifyButton: Button = findViewById<Button>(R.id.notify_button)
        val manageButton: Button = findViewById<Button>(R.id.manage_button)
        val userRoleViewModel: UserRoleViewModel by viewModels()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userRoleViewModel.userRole.observe(this, Observer { role ->
            updateRoleBasedUI(role)
        })
        userRoleViewModel.setUserRole(UserRole.ADMIN)



        feedingButton.setOnClickListener{
            val intent = Intent(this, FeedingActivity::class.java)
            startActivity(intent)
        }
        notifyButton.setOnClickListener{
            showNotifSend()
        }
        manageButton.setOnClickListener {
            val intent = Intent(this, ManageHouseholdActivity::class.java)
            startActivity(intent)
        }


        val adminViewButton = findViewById<Button>(R.id.admin_view_button)
        adminViewButton.setOnClickListener {
            userRoleViewModel.setUserRole(UserRole.ADMIN)
            Log.d(TAG, "adminViewButton clicked")
        }

        val regularViewButton = findViewById<Button>(R.id.regular_view_button)
        regularViewButton.setOnClickListener {
            userRoleViewModel.setUserRole(UserRole.REGULAR)
            Log.d(TAG, "regularViewButton clicked")

        }

        val restrictedViewButton = findViewById<Button>(R.id.restricted_view_button)
        restrictedViewButton.setOnClickListener {
            userRoleViewModel.setUserRole(UserRole.RESTRICTED)
            Log.d(TAG, "restrictedViewButton clicked")
        }
    }

    private fun updateRoleBasedUI(role: UserRole) {
        val notifyButton: Button = findViewById<Button>(R.id.notify_button) //TODO: make this global
        val manageButton: Button = findViewById<Button>(R.id.manage_button)
        when (role) {
            UserRole.ADMIN -> {
                notifyButton.visibility = View.VISIBLE
                manageButton.visibility = View.VISIBLE
            }
            UserRole.REGULAR -> {
                notifyButton.visibility = View.VISIBLE
                manageButton.visibility = View.INVISIBLE
            }
            UserRole.RESTRICTED -> {
                notifyButton.visibility = View.INVISIBLE
                manageButton.visibility = View.INVISIBLE
            }
        }
    }

    private fun showNotifSend() {
        val builder = AlertDialog.Builder(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val users = arrayOf("U1", "U2") //TODO: Get list of users from backend and query if they're restricted

        for (user in users) {
            val userRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            val userNameView = TextView(this).apply {
                text = user
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f)

            }
            val notifyUserButton = Button(this).apply {
                text = "Notify"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener { sendNotif(user) }
            }
            userRow.addView(userNameView)
            userRow.addView(notifyUserButton)

            layout.addView(userRow)
        }


        builder.setView(layout)
        builder.setPositiveButton("Done") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun sendNotif(user: String) { // May pass user as object instead
        //TODO: Implement Firebase api call
    }
}