package com.afterlogic.auroracontacts.presentation.common.databinding.adapters

import android.databinding.BindingAdapter
import android.view.View

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
object ViewBindings {

    @JvmStatic
    @BindingAdapter("onClick")
    fun bindOnClick(view: View, onClick: Runnable?) {
        if (onClick == null) {
            view.setOnClickListener(null)
        } else {
            view.setOnClickListener { onClick.run() }
        }
    }

    @JvmStatic
    @BindingAdapter("focusCommand", "focusTag")
    fun <T> bindFocus(view: View, command: T?, tag: T) {
        if (command == tag) {
            view.requestFocus()
        }
    }

    @JvmStatic
    @BindingAdapter("performClick")
    fun bindPerformClick(view: View, target: View) {
        view.setOnClickListener { target.performClick() }
    }

}