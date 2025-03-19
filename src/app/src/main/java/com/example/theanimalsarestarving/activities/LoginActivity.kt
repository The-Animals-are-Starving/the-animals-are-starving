package com.example.theanimalsarestarving.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.theanimalsarestarving.BuildConfig
import com.example.theanimalsarestarving.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID


class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val activityScope = CoroutineScope(Dispatchers.Main)

    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        skipLogin("bob@gmail.com", "Bob")

        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            promptLogin()
        }

        promptLogin()
    }


    private fun promptLogin() {
        val credentialManager = CredentialManager.create(this)
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(BuildConfig.WEB_CLIENT_ID)
            .setNonce(generateHashedNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        activityScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                handleFailure(e)
            }
        }
    }

    private fun handleFailure(exception: Exception) {
        Log.e("Error", "An error occurred: ${exception.message}")
        // You can also show a Toast or handle the failure differently
        Toast.makeText(this, "Error getting credential", Toast.LENGTH_SHORT).show()
    }

    private fun skipLogin(email: String, name: String) {
        // Save login state
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isLoggedIn", true).putString("userEmail", email).putString("userName", name).apply()

        // Navigate to MainActivity after login
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finish LoginActivity
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d(TAG, "Token: ${googleIdTokenCredential.idToken}")
                        Log.d(TAG, "Name: ${googleIdTokenCredential.displayName.toString()}")

                        val email = googleIdTokenCredential.id
                        Log.d(TAG, "Email: $email")

                        // Save login state
                        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                        sharedPreferences.edit().putBoolean("isLoggedIn", true).putString("userEmail", email).putString("userName", googleIdTokenCredential.displayName.toString()).apply()

                        // Navigate to MainActivity after login
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Finish LoginActivity
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                }
            }
            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun generateHashedNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") {str, it -> str + "%02x".format(it)}
    }
}