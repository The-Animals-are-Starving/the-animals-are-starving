package com.example.theanimalsarestarving.activities

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.network.ApiService
import com.example.theanimalsarestarving.network.NetworkManager
import com.example.theanimalsarestarving.network.RetrofitClient
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.activities.TranslationHelper
import com.example.theanimalsarestarving.repositories.LanguageRepository

import com.example.theanimalsarestarving.utils.AppUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {


    private lateinit var mainRepository: MainRepository
    private lateinit var apiService: ApiService
    lateinit var translationHelper: TranslationHelper  // Declare the TranslationHelper instance

    //buttons
    private lateinit var feedingButton: Button
    private lateinit var notifyButton: Button
    private lateinit var manageButton: Button
    private lateinit var feedingHistoryButton: Button
    private lateinit var logoutButton: Button
    private lateinit var analyticsButton: Button
    private lateinit var translateEnglishButton: Button
    private lateinit var translateFrenchButton: Button


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
        translationHelper = TranslationHelper()

        lifecycleScope.launch {
            try {
                val user = CurrUserRepository.fetchCurrUser(email)

                Log.d(TAG, "USER FETCHED : " + user.toString())

                if (user != null) {
                    CurrUserRepository.setCurrUser(user)
                } else {
                    Log.d(TAG, "Unable to find user in db, redirecting to limbo")
                    redirectToLimbo()
                    return@launch  // Exit early if the user is not found
                }

                // This block will now run after the above logic has finished
                if (CurrUserRepository.getCurrUser()?.householdId.isNullOrEmpty()) {
                    Log.d(TAG, "current user has a null or empty houseId, redirecting to limbo")
                    redirectToLimbo()
                    return@launch  // Exit early if householdId is null or empty
                } else {
                    // set current household TODO: FETCH CURR HOUSEHOLD - do this later if necessary
                    val currHousehold = Household(
                        _id = CurrUserRepository.getCurrUser()?.householdId.toString(),
                        name = "",
                        managerId = "",
                        pets = emptyList(),
                        users = emptyList()
                    )

                    HouseholdRepository.setCurrentHousehold(currHousehold)

                    // Now set content view based on the user's role
                    if (CurrUserRepository.getCurrUser()?.role == "restricted") {
                        val intent = Intent(this@MainActivity, RestrictedMainActivity::class.java)
                        startActivity(intent)
                    } else {
                        setContentView(R.layout.activity_main)

                        setUpButtons()
                        if(CurrUserRepository.getCurrUser()?.role == "normal") {
                            val buttonManage: Button = findViewById(R.id.manage_button)
                            buttonManage.visibility = View.GONE
                            val buttonLog: Button = findViewById(R.id.feeding_history_button)
                            buttonLog.visibility = View.GONE
                            val buttonAnalytic: Button = findViewById(R.id.analytics_button)
                            buttonAnalytic.visibility = View.GONE
                        }
                        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                            insets
                        }

                    }
                }

            } catch (e: HttpException) {
                Log.e(TAG, "HttpException fetching user: ${e.message}")
            } catch (e: IOException) {
                Log.e("TAG", "IOException fetching user: ${e.message}")
            }

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

                val repository = MainRepository(NetworkManager.apiService)
                repository.updateUserToken(email, token) { success ->
                    if (success) {
                        Log.d("UpdateUserToken", "User token updated successfully")
                    } else {
                        Log.e("MainRepository", "Error: Failed to update user token")
                    }
                }
            })
        }

    }

    private fun setUpButtons(){

        feedingButton = findViewById(R.id.feed_button)
        notifyButton = findViewById(R.id.notify_button)
        manageButton = findViewById(R.id.manage_button)
        feedingHistoryButton = findViewById(R.id.feeding_history_button)
        logoutButton = findViewById(R.id.logoutButton)
        analyticsButton = findViewById(R.id.analytics_button)
        translateFrenchButton = findViewById(R.id.translate_fr_button)
        translateEnglishButton = findViewById(R.id.translate_en_button)

        feedingButton.setOnClickListener {
            val intent = Intent(this, FeedingActivity::class.java)
            intent.putExtra("translationHelperVar", translationHelper)
            startActivity(intent)

        }
        notifyButton.setOnClickListener {
            showNotifSend()
        }
        manageButton.setOnClickListener {
            val intent = Intent(this, ManageHouseholdActivity::class.java)
            intent.putExtra("translationHelperVar", translationHelper)
            startActivity(intent)
        }
        feedingHistoryButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("translationHelperVar", translationHelper)
            startActivity(intent)
        }
        analyticsButton.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            intent.putExtra("translationHelperVar", translationHelper)
            startActivity(intent)
        }

        translateFrenchButton.setOnClickListener {
            Log.d(TAG, "currLanguage: ${LanguageRepository.language}")
            // Collect all views dynamically and change the language to French
            val allViews = translationHelper.getAllViews(findViewById(R.id.main)) // Replace with actual layout ID
            translationHelper.changeLanguage("fr", lifecycleScope, allViews)
        }

        translateEnglishButton.setOnClickListener {
            // Collect all views dynamically and change the language to English
            val allViews = translationHelper.getAllViews(findViewById(R.id.main)) // This will now return a List<View>
            translationHelper.changeLanguage("en", lifecycleScope, allViews)
        }



        logoutButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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
        val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val currentUserName = sharedPreferences.getString("name", "").toString()

        val repository = MainRepository(apiService)
        repository.getAllUsers(HouseholdRepository.getCurrentHousehold()?._id.toString()) { users ->
            if (users != null) {
                val otherUsers = users.filter { it.name != currentUserName }

                if (otherUsers.isEmpty()) {
                    val noticeText = "No one else is in this household. You are the only one."
                    val noticeView = TextView(this).apply {
                        text = noticeText
                        textSize = 20f
                    }
                    layout.addView(noticeView)
                } else {
                    for (user in otherUsers) {
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
                            text = getString(R.string.notify_text)
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
                AppUtils.alertMessage(this, "Failed to fetch users. Please try again.")
            }
        }

        builder.setView(layout)
        builder.setPositiveButton("Done") { dialog, _ -> dialog.dismiss() }

        builder.show()

    }

    private fun sendNotif(email: String) { // May pass user as object instead
        apiService.sendNotification(email).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Notification sent.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to send notification.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

}
