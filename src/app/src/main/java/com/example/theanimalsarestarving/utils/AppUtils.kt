package com.example.theanimalsarestarving.utils

import android.app.AlertDialog
import android.content.Context

object AppUtils {

    fun alertMessage(context: Context, message: String) {
        val warning = AlertDialog.Builder(context)
        warning.setTitle("Error")
        warning.setMessage(message)
        warning.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        warning.show()
    }

}