package com.example.theanimalsarestarving.activities

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.example.theanimalsarestarving.BuildConfig
import com.example.theanimalsarestarving.R
import com.example.theanimalsarestarving.repositories.LanguageRepository
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Serializable

private const val TAG = "TranslationHelper"

class TranslationHelper : Serializable {

    // Suspend function for translating a single string
    suspend fun translateString(value: String, targetLanguage: String): String {
        return withContext(Dispatchers.IO) {
            val translate = TranslateOptions.newBuilder()
                .setApiKey(BuildConfig.GOOGLE_TRANSLATE_API) // Replace with your actual API key
                .build()
                .service

            val translation: Translation = translate.translate(
                value,
                Translate.TranslateOption.targetLanguage(targetLanguage)
            )
            val translatedText = translation.translatedText

            val decodedText = HtmlCompat.fromHtml(translatedText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

            decodedText
        }
    }

    // Dynamically translate and update all text-related UI elements in the current layout
    suspend fun translateAndUpdateUI(targetLanguage: String, rootView: View) {
        // Get all views to translate
        val allViews = getAllViews(rootView)

        // Translate all strings asynchronously and update the views
        for (view in allViews) {
            when (view) {
                is TextView -> {
                    val originalText = view.text.toString()
                    val translatedText = translateString(originalText, targetLanguage)
                    view.text = translatedText
                }

                is Button -> {
                    val originalText = view.text.toString()
                    val translatedText = translateString(originalText, targetLanguage)
                    view.text = translatedText
                }

                is EditText -> {
                    val originalHint = view.hint.toString()
                    val translatedHint = translateString(originalHint, targetLanguage)
                    view.setHint(translatedHint)
                }
            }
        }
    }


    // Get all views in the layout for translation
     fun getAllViews(root: View): List<View> {
        val views = mutableListOf<View>()
        if (root is TextView || root is Button || root is EditText) {
            views.add(root)
        }

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                views.addAll(getAllViews(child))
            }
        }

        return views
    }

    // Change language dynamically for the given views list
    fun changeLanguage(
        language: String,
        lifecycleScope: LifecycleCoroutineScope,
        views: List<View>
    ) {

        Log.d(TAG, "setting language: $language")

        lifecycleScope.launch {
            // Perform translation in the background
            for (view in views) {
                when (view) {
                    is TextView -> {
                        val translatedText = translateString(view.text.toString(), language)
                        view.text = translatedText
                    }

                    is Button -> {
                        val translatedText = translateString(view.text.toString(), language)
                        view.text = translatedText
                    }

                    is EditText -> {
                        val translatedHint = translateString(view.hint.toString(), language)
                        view.setHint(translatedHint)
                    }
                }
            }
            LanguageRepository.language = language
            Log.d(TAG, "Language Set: ${LanguageRepository.language}")
        }
    }

    fun updateLanguageUI(helper: TranslationHelper, view: View, lifecycleScope: LifecycleCoroutineScope){
        val allViews = helper.getAllViews(view) // Replace with actual layout ID
        helper.changeLanguage(LanguageRepository.language, lifecycleScope, allViews)
    }
}

