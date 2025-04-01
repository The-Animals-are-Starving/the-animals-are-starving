package com.example.theanimalsarestarving.activities


import android.content.Context
import android.content.res.Resources
import android.widget.Button
import android.widget.TextView
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.activities.MainActivity
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import java.util.*

class TranslationHelper(private val context: Context) {

    private val TRANSLATE_API_KEY = "somekey"

    fun getStringFromResources(stringId: Int): String {
        return context.getString(stringId)
    }

    // Translate a single string using Google Translate API
    fun translateString(value: String, targetLanguage: String): String {
        val apiKey = "TRANSLATE_API_KEY"  // Replace with your actual API key
        val translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().service
        val translation: Translation = translate.translate(value, Translate.TranslateOption.targetLanguage(targetLanguage))
        return translation.translatedText
    }

    // Translate all strings and update the UI
    fun translateAndUpdateUI(targetLanguage: String) {
        // Translate individual strings
        val titleText = getStringFromResources(R.string.the_animals_are_starving)
        val feedText = getStringFromResources(R.string.feed_da_dawg)
        val notifyText = getStringFromResources(R.string.notify_other_users)
        val manageText = getStringFromResources(R.string.manage_household)
        val historyText = getStringFromResources(R.string.feeding_history)
        val analyticsText = getStringFromResources(R.string.analytics)
        val frenchText = getStringFromResources(R.string.french)
        val logoutText = getStringFromResources(R.string.logout)

        // Translate each string
        val translatedTitle = translateString(titleText, targetLanguage)
        val translatedFeed = translateString(feedText, targetLanguage)
        val translatedNotify = translateString(notifyText, targetLanguage)
        val translatedManage = translateString(manageText, targetLanguage)
        val translatedHistory = translateString(historyText, targetLanguage)
        val translatedAnalytics = translateString(analyticsText, targetLanguage)
        val translatedFrench = translateString(frenchText, targetLanguage)
        val translatedLogout = translateString(logoutText, targetLanguage)

        // Update UI elements with translated text
        updateUIWithTranslatedStrings(
            translatedTitle, translatedFeed, translatedNotify, translatedManage,
            translatedHistory, translatedAnalytics, translatedFrench, translatedLogout
        )
    }

    // Update the UI elements with the translated strings
    fun updateUIWithTranslatedStrings(
        translatedTitle: String,
        translatedFeed: String,
        translatedNotify: String,
        translatedManage: String,
        translatedHistory: String,
        translatedAnalytics: String,
        translatedFrench: String,
        translatedLogout: String
    ) {
        val mainActivity = context as MainActivity

        // Find your UI elements and update their text
        val textViewTitle = mainActivity.findViewById<TextView>(R.id.title)
        val buttonFeed = mainActivity.findViewById<Button>(R.id.feed_button)
        val buttonNotify = mainActivity.findViewById<Button>(R.id.notify_button)
        val buttonManage = mainActivity.findViewById<Button>(R.id.manage_button)
        val buttonHistory = mainActivity.findViewById<Button>(R.id.feeding_history_button)
        val buttonAnalytics = mainActivity.findViewById<Button>(R.id.analytics_button)
        val buttonTranslate = mainActivity.findViewById<Button>(R.id.translate_button)
        val buttonLogout = mainActivity.findViewById<Button>(R.id.logoutButton)

        // Update the UI text with translated values
        textViewTitle.text = translatedTitle
        buttonFeed.text = translatedFeed
        buttonNotify.text = translatedNotify
        buttonManage.text = translatedManage
        buttonHistory.text = translatedHistory
        buttonAnalytics.text = translatedAnalytics
        buttonTranslate.text = translatedFrench
        buttonLogout.text = translatedLogout
    }

    // Function to handle language change
    public fun changeLanguage(languageCode: String) {
        translateAndUpdateUI(languageCode)
    }
}
