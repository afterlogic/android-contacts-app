package com.afterlogic.auroracontacts.presentation.common.databinding.adapters

import android.databinding.BindingAdapter
import android.view.View

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
object ViewBindings {

    @JvmStatic
    @BindingAdapter("focusCommand", "focusTag")
    fun <T> bindFocus(view: View, command: T?, tag: T) {
        if (command == tag) {
            view.requestFocus()
        }
    }

}