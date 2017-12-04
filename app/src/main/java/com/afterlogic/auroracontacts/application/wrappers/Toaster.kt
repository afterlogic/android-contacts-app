package com.afterlogic.auroracontacts.application.wrappers

import android.widget.Toast
import com.afterlogic.auroracontacts.application.App

import javax.inject.Inject

/**
 * Created by aleksandrcikin on 29.04.17.
 */

class Toaster @Inject
constructor(private val context: App) {

    fun showLong(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun showLong(textId: Int) {
        Toast.makeText(context, textId, Toast.LENGTH_LONG).show()
    }


    fun showShort(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun showShort(textId: Int) {
        Toast.makeText(context, textId, Toast.LENGTH_LONG).show()
    }

}
