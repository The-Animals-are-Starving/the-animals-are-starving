package com.example.theanimalsarestarving.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.activities.helper.NetworkInitializer
import com.example.theanimalsarestarving.network.NetworkManager
import com.example.theanimalsarestarving.repositories.CurrUserRepository
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.activities.helper.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import org.apache.http.HttpException
import java.io.IOException

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var mainRepository: MainRepository
    private lateinit var apiService: com.example.theanimalsarestarving.network.ApiService
    private lateinit var userApiService: com.example.theanimalsarestarving.network.UserApiService
    lateinit var translationHelper: TranslationHelper

    private lateinit var feedingButton: Button
    private lateinit var notifyButton: Button
    private lateinit var manageButton: Button
    private lateinit var feedingHistoryButton: Button
    private lateinit var logoutButton: Button
    private lateinit var analyticsButton: Button
    private lateinit var translateFrenchButton: Button
    private lateinit var translateEnglishButton: Button

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isUserLoggedIn()) {
            redirectToLogin()
        }

        enableEdgeToEdge()
        Log.d(TAG, "onCreate")

        // Initialize network components from helper
        val (api, repository) = NetworkInitializer.init()
        apiService = api
        mainRepository = repository
        userApiService = NetworkManager.userApiService

        val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val email = sharedPreferences.getString("userEmail", "").toString()

        translationHelper = TranslationHelper()

        initializeUserAndSetupUI(email)
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.askNotificationPermission(this, requestPermissionLauncher)
    }

    private fun initializeUserAndSetupUI(email: String) {
        lifecycleScope.launch {
            try {
                val user = CurrUserRepository.fetchCurrUser(email)
                Log.d(TAG, "USER FETCHED: $user")
                if (user == null || user.householdId.isNullOrEmpty()) {
                    redirectToLimbo()
                    return@launch
                }
                CurrUserRepository.setCurrUser(user)
                val currHousehold = Household(
                    _id = user.householdId,
                    name = "",
                    managerId = "",
                    pets = emptyList(),
                    users = emptyList()
                )
                HouseholdRepository.setCurrentHousehold(currHousehold)
                if (user.role == "restricted") {
                    val intent = Intent(this@MainActivity, RestrictedMainActivity::class.java)
                    intent.putExtra("translationHelperVar", translationHelper)
                    startActivity(intent)

                    return@launch
                } else {
                    setContentView(R.layout.activity_main)
                    setUpButtons()
                    applyWindowInsets()
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HttpException fetching user: ${e.message}")
            } catch (e: IOException) {
                Log.e("TAG", "IOException fetching user: ${e.message}")
            }
            updateFirebaseMessagingToken(email)
        }
    }

    private fun setUpButtons() {
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
            NotificationHelper.showNotifSend(this, apiService, userApiService)
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
            val allViews = translationHelper.getAllViews(findViewById(R.id.main))
            translationHelper.changeLanguage("fr", lifecycleScope, allViews)
        }
        translateEnglishButton.setOnClickListener {
            val allViews = translationHelper.getAllViews(findViewById(R.id.main))
            translationHelper.changeLanguage("en", lifecycleScope, allViews)
        }
        logoutButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateFirebaseMessagingToken(email: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, getString(R.string.msg_token_fmt, token))
            // Update token using your repository logic
            mainRepository.updateUserToken(email, token) { success ->
                if (success) Log.d("UpdateUserToken", "User token updated successfully")
                else Log.e("MainRepository", "Error: Failed to update user token")
            }
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun redirectToLimbo() {
        startActivity(Intent(this, CreateHouseholdActivity::class.java))
        finish()
    }
}
