package com.example.theanimalsarestarving.helpers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.utils.AppUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.theanimalsarestarving.network.ApiService

object NotificationHelper {

    fun askNotificationPermission(activity: Activity, permissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted; do nothing.
                }
                activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    AlertDialog.Builder(activity)
                        .setTitle("Notification Permission Required")
                        .setMessage("This feature is critical for alerting you when your pet needs feeding.")
                        .setPositiveButton("OK") { dialog, _ ->
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            dialog.dismiss()
                        }
                        .setNegativeButton("No thanks") { dialog, _ ->
                            Toast.makeText(activity, "Notifications will be disabled.", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
                else -> {
                    val sharedPref: SharedPreferences = activity.getSharedPreferences("permission_prefs", Context.MODE_PRIVATE)
                    val hasRequested = sharedPref.getBoolean("requestedPostNotifications", false)
                    if (!hasRequested) {
                        sharedPref.edit().putBoolean("requestedPostNotifications", true).apply()
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        AlertDialog.Builder(activity)
                            .setTitle("Enable Notifications in Settings")
                            .setMessage("Notifications are a key feature. Please go to Settings > Apps > YourApp > Permissions and enable notifications.")
                            .setPositiveButton("Open Settings") { dialog, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.fromParts("package", activity.packageName, null)
                                activity.startActivity(intent)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()
                    }
                }
            }
        }
    }

    fun showNotifSend(context: Context, apiService: ApiService) {
        val builder = AlertDialog.Builder(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentUserName = sharedPreferences.getString("name", "").toString()

        // Fetch users from the repository (this example uses a callback)
        val repository = com.example.theanimalsarestarving.repositories.MainRepository(apiService)
        repository.getAllUsers(HouseholdRepository.getCurrentHousehold()?._id.toString()) { users ->
            if (users != null) {
                val otherUsers = users.filter { it.name != currentUserName }
                if (otherUsers.isEmpty()) {
                    val noticeView = TextView(context).apply {
                        text = "You are alone."
                        textSize = 20f
                    }
                    layout.addView(noticeView)
                } else {
                    for (user in otherUsers) {
                        val userRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
                        val userNameView = TextView(context).apply {
                            text = user.name
                            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                        }
                        val notifyUserButton = Button(context).apply {
                            text = context.getString(R.string.notify_text)
                            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            setOnClickListener { sendNotif(context, apiService, user.email) }
                        }
                        userRow.addView(userNameView)
                        userRow.addView(notifyUserButton)
                        layout.addView(userRow)
                    }
                }
            } else {
                AppUtils.alertMessage(context, "Failed to fetch users. Please try again.")
            }
        }

        builder.setView(layout)
        builder.setPositiveButton("Done") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    fun sendNotif(context: Context, apiService: ApiService, email: String) {
        apiService.sendNotification(email).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Notification sent.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to send notification.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
