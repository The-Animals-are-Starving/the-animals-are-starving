package com.example.theanimalsarestarving.activities

import com.example.theanimalsarestarving.models.UserRoleViewModel
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
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.UserRole
import com.example.theanimalsarestarving.network.ApiService
import com.example.theanimalsarestarving.network.MainRepository
import com.example.theanimalsarestarving.network.RetrofitClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {


    private val TAG = "MainActivity"
    private lateinit var mainRepository: MainRepository

    //buttons
    private lateinit var feedingButton: Button
    private lateinit var notifyButton: Button
    private lateinit var manageButton: Button
    private lateinit var feedingHistoryButton: Button
    private lateinit var adminViewButton: Button
    private lateinit var regularViewButton: Button
    private lateinit var restrictedViewButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        retrofitInit()


        Log.d(TAG, "onCreate")

        feedingButton = findViewById(R.id.feed_button)
        notifyButton = findViewById(R.id.notify_button)
        manageButton = findViewById(R.id.manage_button)
        feedingHistoryButton = findViewById(R.id.feeding_history_button)
        adminViewButton = findViewById(R.id.admin_view_button)
        regularViewButton = findViewById(R.id.regular_view_button)
        restrictedViewButton = findViewById(R.id.restricted_view_button)

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
        feedingHistoryButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }


        adminViewButton.setOnClickListener {
            userRoleViewModel.setUserRole(UserRole.ADMIN)
            Log.d(TAG, "adminViewButton clicked")
        }

        regularViewButton.setOnClickListener {
            userRoleViewModel.setUserRole(UserRole.REGULAR)
            Log.d(TAG, "regularViewButton clicked")

        }

        restrictedViewButton.setOnClickListener {
            userRoleViewModel.setUserRole(UserRole.RESTRICTED)
            Log.d(TAG, "restrictedViewButton clicked")
        }
    }

    private fun updateRoleBasedUI(role: UserRole) {
        when (role) {
            UserRole.ADMIN -> {
                notifyButton.visibility = View.VISIBLE
                manageButton.visibility = View.VISIBLE
                feedingHistoryButton.visibility = View.VISIBLE
            }
            UserRole.REGULAR -> {
                notifyButton.visibility = View.VISIBLE
                manageButton.visibility = View.INVISIBLE
                feedingHistoryButton.visibility = View.INVISIBLE

            }
            UserRole.RESTRICTED -> {
                notifyButton.visibility = View.INVISIBLE
                manageButton.visibility = View.INVISIBLE
                feedingHistoryButton.visibility = View.INVISIBLE

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


    private fun retrofitInit() {
        Log.d(TAG,"retrofitInit()")
        // Initialize Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitClient.baseUrl)  // Base URL from RetrofitClient
            .addConverterFactory(GsonConverterFactory.create())  // Gson converter for JSON response
            .build()

        // Initialize ApiService
        val apiService = retrofit.create(ApiService::class.java)

        // Initialize MainRepository with ApiService
        val mainRepository = MainRepository(apiService)

        // Call the getUser method with a callback to handle the response
        val email = "test@gmail.com"

        // Make an asynchronous API call
        mainRepository.getUser(email) { user ->
            if (user != null) {
                // Log the user details if fetched successfully
                Log.d(TAG, "Fetched user: $user")
            } else {
                // Log if no user was found or an error occurred
                Log.d(TAG, "No user found or error occurred.")
            }
        }
    }
}
