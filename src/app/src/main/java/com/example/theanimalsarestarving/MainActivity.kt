package com.example.theanimalsarestarving

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val manageHouseholdButton = findViewById<Button>(R.id.ManageButton)

        manageHouseholdButton.setOnClickListener {
            val intent = Intent(this, ManageHouseholdActivity::class.java)
            startActivity(intent)
        }

        val restrictedViewButton = findViewById<Button>(R.id.restricted_view_button)

        restrictedViewButton.setOnClickListener {
            val intent = Intent(this, RestrictedActivity::class.java)
            startActivity(intent)
        }

        val notificationButton = findViewById<Button>(R.id.notif_button)

        notificationButton.setOnClickListener{showNotifSend()}
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