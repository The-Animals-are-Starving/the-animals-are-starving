package com.example.theanimalsarestarving.activities

import android.os.Bundle
import android.widget.TableLayout
import android.util.Log
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.models.Household
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.MainRepository
import com.example.theanimalsarestarving.utils.AppUtils

class AnalyticsActivity : AppCompatActivity() {

    lateinit var translationHelper: TranslationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.analytics_activity)
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            translationHelper = intent.getSerializableExtra("translationHelperVar") as TranslationHelper
        }
        val householdId = HouseholdRepository.getCurrentHousehold()?._id.toString()

        refreshAnalytics(householdId)
        translationHelper.updateLanguageUI(translationHelper, findViewById(R.id.analytics_activity), lifecycleScope)

    }

    private fun refreshAnalytics(householdId: String) {
        val anomalyTable = findViewById<TableLayout>(R.id.analyticsBox)
        val repository = MainRepository(apiService)

        repository.getFeedingAnomalies(householdId) { anomalies ->
            Log.d("Analytics", anomalies.toString())
            anomalyTable.removeAllViews()

            val headerRow = TableRow(this).apply {
                setPadding(8, 8, 8, 8)
            }

            listOf("Pet Name", "Feeding Amount", "Feeding Time", "Avg Amount", "Feeding Count").forEach { header ->
                val headerView = TextView(this@AnalyticsActivity).apply {
                    text = header
                    textSize = 18f
                    setPadding(10, 10, 10, 10)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f) // Ensure equal spacing
                }
                headerRow.addView(headerView)
            }
            anomalyTable.addView(headerRow)

            if (anomalies != null) {
                if (anomalies.isEmpty()) {
                    val noticeText = TextView(this).apply {
                        text = getString(R.string.no_logs_text)
                        textSize = 20f
                        setPadding(10, 10, 10, 10)
                    }
                    anomalyTable.addView(noticeText)
                } else {
                    for (anomaly in anomalies) {
                        val anomalyRow = TableRow(this).apply {
                            setPadding(10, 10, 10, 10)
                        }

                        val petNameView = TextView(this).apply {
                            text = anomaly.pet
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        }

                        val largeDeviationView = TextView(this).apply {
                            text = if (anomaly.largeDeviation) "⚠️" else "✔️"
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                            gravity = Gravity.CENTER
                        }

                        val significantlyLateView = TextView(this).apply {
                            text = if (anomaly.significantlyLate) "⚠️" else "✔️"
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                            gravity = Gravity.CENTER
                        }

                        val averageAmountView = TextView(this).apply {
                            text = anomaly.averageAmount.toString()
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                        }

                        val feedingCountView = TextView(this).apply {
                            text = anomaly.feedingCount.toString()
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                            gravity = Gravity.END
                        }

                        anomalyRow.addView(petNameView)
                        anomalyRow.addView(largeDeviationView)
                        anomalyRow.addView(significantlyLateView)
                        anomalyRow.addView(averageAmountView)
                        anomalyRow.addView(feedingCountView)

                        anomalyTable.addView(anomalyRow)
                    }

                }
            } else {
                AppUtils.alertMessage(this, "Failed to fetch analytics. Please try again.")
            }

        }
    }
}
