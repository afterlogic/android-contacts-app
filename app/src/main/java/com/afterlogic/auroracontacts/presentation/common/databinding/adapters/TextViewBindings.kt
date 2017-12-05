package com.afterlogic.auroracontacts.presentation.common.databinding.adapters

import android.databinding.BindingAdapter
import android.widget.TextView

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
object TextViewBindings {

    @JvmStatic
    @BindingAdapter("error")
    fun bindError(textView: TextView, error: String?) {
        textView.error = error
    }

}