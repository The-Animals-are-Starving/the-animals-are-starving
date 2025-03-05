package com.example.theanimalsarestarving.activities

import com.example.theanimalsarestarving.models.UserRoleViewModel
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import android.Manifest
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.activities.FeedingActivity.Companion
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.models.User
import com.example.theanimalsarestarving.models.UserRole
import com.example.theanimalsarestarving.network.ApiService
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.network.NetworkManager
import com.example.theanimalsarestarving.network.RetrofitClient
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.PetRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {


    private val TAG = "MainActivity"
    private lateinit var mainRepository: MainRepository
    private lateinit var apiService: ApiService

    //buttons
    private lateinit var feedingButton: Button
    private lateinit var notifyButton: Button
    private lateinit var manageButton: Button
    private lateinit var feedingHistoryButton: Button
    private lateinit var adminViewButton: Button
    private lateinit var regularViewButton: Button
    private lateinit var restrictedViewButton: Button
    private lateinit var openCreateHouseholdButton: Button
    private lateinit var logoutButton: Button

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // can do notifications stuff now
        } else {
            Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        askNotificationPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (!isUserLoggedIn()) {
            redirectToLogin()
        }

        enableEdgeToEdge()

        Log.d(TAG, "onCreate")

        retrofitInit()

        val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val email = sharedPreferences.getString("userEmail", "").toString()
        val name = sharedPreferences.getString("userName", "").toString()

        //TODO: !!FROM TJ!! REMOVE THIS HARD CODED EMAIL IF YOU WANT TO TEST
//        val email = "test1234@gmail.com"

        lifecycleScope.launch {
            try {
                val user = CurrUserRepository.fetchCurrUser(email)

                Log.d(TAG, "USER FETCHED : " + user.toString())

                if (user != null) {
                    CurrUserRepository.setCurrUser(user)
                } else {
                    Log.d(TAG, "Unable to find user in db, redirecting to limbo")
                    redirectToLimbo()
                }

                // This block will now run after the above logic has finished
                if (CurrUserRepository.getCurrUser()?.householdId.isNullOrEmpty()) {
                    Log.d(TAG, "current user has a null or empty houseid, redirecting to limbo")
                    redirectToLimbo()
                } else {
                    //set current household TODO: FETCH CURR HOUSEHOLD - do this later if necessary
                    val currHousehold = Household(
                        _id = CurrUserRepository.getCurrUser()?.householdId.toString(),
                        name = "",
                        managerId = "",
                        pets = emptyList(),
                        users = emptyList()
                    )

                    HouseholdRepository.setCurrentHousehold(currHousehold)

                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user: ${e.message}")
            }
        }


        setContentView(R.layout.activity_main)


        Log.d(TAG, "Current Household: ${HouseholdRepository.getCurrentHousehold()}\n Current User: ${CurrUserRepository.getCurrUser()}\n Current pets: ${PetRepository.getPets()}")


        feedingButton = findViewById(R.id.feed_button)
        notifyButton = findViewById(R.id.notify_button)
        manageButton = findViewById(R.id.manage_button)
        feedingHistoryButton = findViewById(R.id.feeding_history_button)
        adminViewButton = findViewById(R.id.admin_view_button)
        regularViewButton = findViewById(R.id.regular_view_button)
        restrictedViewButton = findViewById(R.id.restricted_view_button)
        openCreateHouseholdButton = findViewById(R.id.openCreateHouseholdButton)
        logoutButton = findViewById(R.id.logoutButton)

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

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        feedingButton.setOnClickListener {
            val intent = Intent(this, FeedingActivity::class.java)
            startActivity(intent)
        }
        notifyButton.setOnClickListener {
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
        openCreateHouseholdButton.setOnClickListener() {
            val intent = Intent(this, CreateHouseholdActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener() {
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted; do nothing.
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    AlertDialog.Builder(this)
                        .setTitle("Notification Permission Required")
                        .setMessage("This feature is critical for alerting you when your pet needs feeding.")
                        .setPositiveButton("OK") { dialog, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            dialog.dismiss()
                        }
                        .setNegativeButton("No thanks") { dialog, _ ->
                            Toast.makeText(this, "Notifications will be disabled.", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
                else -> {
                    val sharedPref = getSharedPreferences("permission_prefs", MODE_PRIVATE)
                    val hasRequested = sharedPref.getBoolean("requestedPostNotifications", false)
                    if (!hasRequested) {
                        sharedPref.edit().putBoolean("requestedPostNotifications", true).apply()
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("Enable Notifications in Settings")
                            .setMessage("Notifications are a key feature. Please go to Settings > Apps > YourApp > Permissions and enable notifications.")
                            .setPositiveButton("Open Settings") { dialog, _ ->
                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = android.net.Uri.fromParts("package", packageName, null)
                                startActivity(intent)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                }
            }
        }
    }

    private fun showNotifSend() {
        val builder = AlertDialog.Builder(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val repository = MainRepository(apiService)
        repository.getAllUsers(HouseholdRepository.getCurrentHousehold()?._id.toString()) { users ->
            if (users != null) {
                if (users.isEmpty()) { //not working atm dunno why
                    val noticeText = "No Users in Household"
                    val noticeView = TextView(this).apply {
                        text = noticeText
                        textSize = 20f
                    }
                    layout.addView(noticeView)
                } else {
                    for (user in users) {
                        val userRow = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                        }
                        val userNameView = TextView(this).apply {
                            text = user.name
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )

                        }
                        val notifyUserButton = Button(this).apply {
                            text = "Notify"
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setOnClickListener { sendNotif(user.email) }
                        }
                        userRow.addView(userNameView)
                        userRow.addView(notifyUserButton)

                        layout.addView(userRow)
                    }
                }

            } else {
                alertMessage("Failed to fetch users. Please try again.", layout)
            }
        }


        builder.setView(layout)
        builder.setPositiveButton("Done") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun sendNotif(email: String) { // May pass user as object instead
        //TODO: Implement Firebase api call
    }


    private fun retrofitInit() {
        Log.d(TAG, "retrofitInit()")

        // Initialize Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitClient.baseUrl)  // Base URL from RetrofitClient
            .addConverterFactory(GsonConverterFactory.create())  // Gson converter for JSON response
            .build()

        // Initialize ApiService
        apiService = retrofit.create(ApiService::class.java)

        // Initialize MainRepository with ApiService
        mainRepository = MainRepository(apiService)

        // Initialize the singleton with the instances
        NetworkManager.initialize(apiService, mainRepository)
//
//        // Call the getUser method with a callback to handle the response
//        val email = "test@gmail.com"
//
//        // Make an asynchronous API call
//        mainRepository.getUser(email) { user ->
//            if (user != null) {
//                Log.d(TAG, "retrofitInit: Fetched user: $user")
//            } else {
//                Log.d(TAG, "retrofitInit: No user found or error occurred.")
//            }
//        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false) // Default is false (not logged in)
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity so user can't come back by pressing back
    }

    private fun redirectToLimbo() {
        val intent = Intent(this, CreateHouseholdActivity::class.java)
        startActivity(intent)
        finish() // Finish MainActivity so user can't come back by pressing back
    }

    private fun alertMessage(message: String, container: LinearLayout) {
        val warning = AlertDialog.Builder(this)
        warning.setTitle("Error")
        warning.setMessage(message)
        warning.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        warning.show()
    }
}
