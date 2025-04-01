package com.example.theanimalsarestarving.activities
import android.content.Context
import android.widget.Button
import android.widget.TextView
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.activities.MainActivity
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranslationHelper(private val context: Context) {

    // Suspend function for translating a single string
    private val TRANSLATE_API_KEY = "AIzaSyD4ykkYCFgjq6OdthtgF_FhVWUDdbwAQtM"

    suspend fun translateString(value: String, targetLanguage: String): String {
        return withContext(Dispatchers.IO) {
            val translate = TranslateOptions.newBuilder()
                .setApiKey(TRANSLATE_API_KEY) // Replace with your actual API key
                .build()
                .service

            val translation: Translation = translate.translate(
                value,
                Translate.TranslateOption.targetLanguage(targetLanguage)
            )
            translation.translatedText
        }
    }
    // Suspend function to translate all strings
    suspend fun translateAndUpdateUI(targetLanguage: String) {
        // Translate individual strings asynchronously
        val titleText = getStringFromResources(R.string.the_animals_are_starving)
        val feedText = getStringFromResources(R.string.feed_da_dawg)
        val notifyText = getStringFromResources(R.string.notify_other_users)
        val manageText = getStringFromResources(R.string.manage_household)
        val historyText = getStringFromResources(R.string.feeding_history)
        val analyticsText = getStringFromResources(R.string.analytics)
        val logoutText = getStringFromResources(R.string.logout)

        val translatedTitle = translateString(titleText, targetLanguage)
        val translatedFeed = translateString(feedText, targetLanguage)
        val translatedNotify = translateString(notifyText, targetLanguage)
        val translatedManage = translateString(manageText, targetLanguage)
        val translatedHistory = translateString(historyText, targetLanguage)
        val translatedAnalytics = translateString(analyticsText, targetLanguage)
        val translatedLogout = translateString(logoutText, targetLanguage)

        // Wait for all translations to complete and then update the UI
        updateUIWithTranslatedStrings(
            translatedTitle, translatedFeed, translatedNotify, translatedManage,
            translatedHistory, translatedAnalytics, translatedLogout
        )
    }

    private fun getStringFromResources(stringId: Int): String {
        return context.getString(stringId)
    }

    // Update the UI elements with the translated strings
    private suspend fun updateUIWithTranslatedStrings(
        translatedTitle: String,
        translatedFeed: String,
        translatedNotify: String,
        translatedManage: String,
        translatedHistory: String,
        translatedAnalytics: String,
        translatedLogout: String
    ) {
        // Switch back to the main thread to update the UI
        withContext(Dispatchers.Main) {
            val mainActivity = context as MainActivity

            // Find your UI elements and update their text
            val textViewTitle = mainActivity.findViewById<TextView>(R.id.title)
            val buttonFeed = mainActivity.findViewById<Button>(R.id.feed_button)
            val buttonNotify = mainActivity.findViewById<Button>(R.id.notify_button)
            val buttonManage = mainActivity.findViewById<Button>(R.id.manage_button)
            val buttonHistory = mainActivity.findViewById<Button>(R.id.feeding_history_button)
            val buttonAnalytics = mainActivity.findViewById<Button>(R.id.analytics_button)

            val buttonLogout = mainActivity.findViewById<Button>(R.id.logoutButton)

            // Update the UI text with translated values
            textViewTitle.text = translatedTitle
            buttonFeed.text = translatedFeed
            buttonNotify.text = translatedNotify
            buttonManage.text = translatedManage
            buttonHistory.text = translatedHistory
            buttonAnalytics.text = translatedAnalytics
            buttonLogout.text = translatedLogout
        }
    }

    // Function to handle language change
    fun changeLanguage(languageCode: String, lifecycleScope: CoroutineScope) {
        // Launch a coroutine to handle translation and UI update
        lifecycleScope.launch {
            translateAndUpdateUI(languageCode)
        }
    }
}
