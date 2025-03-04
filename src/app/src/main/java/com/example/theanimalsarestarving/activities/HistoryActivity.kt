package com.example.theanimalsarestarving.activities

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.network.NetworkManager.apiService
import com.example.theanimalsarestarving.repositories.HouseholdRepository
import com.example.theanimalsarestarving.repositories.MainRepository

class HistoryActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activity)

        val historyTextView = findViewById<LinearLayout>(R.id.petContainer)

        val householdId = HouseholdRepository.getCurrentHousehold().toString()

        val repository = MainRepository(apiService)

        fun refreshHistory(householdId: String) {
            repository.getLogs(householdId) { logs ->
                if (logs != null) {
                    if (logs.isEmpty()) {
                        val noticeText = "No Logs Available"
                        val textView = TextView(this).apply {
                            text = noticeText
                            textSize = 30f
                        }
                        historyTextView.addView(textView)
                    } else {
                        for (log in logs) {
                            val logRow = LinearLayout(this).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                setPadding(10, 10, 10, 10)
                            }
                            val logView = TextView(this).apply {
                                text = "Pet: ${log.petId?.name ?: "Unknown Pet"}," +
                                        "Fed by: ${log.userId?.name ?: "Unknown User"}, " +
                                        "Time: ${log.timestamp}"
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                            }

                            logRow.addView(logView)
                            historyTextView.addView(logRow)
                        }
                    }
                }
            }
    }   }
}