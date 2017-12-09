package com.afterlogic.auroracontacts.presentation.common.databinding.adapters

import android.databinding.BindingAdapter
import android.graphics.drawable.Animatable
import android.widget.ImageView

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

object ImageViewBindings {

    @JvmStatic
    @BindingAdapter("runDrawableAnimation")
    fun bindSyncAnimation(fab: ImageView, run: Boolean) {

        val drawable = fab.drawable ?: return
        drawable as? Animatable ?: return

        if (run) {
            drawable.start()
        } else {
            drawable.stop()
            drawable.level = 0
        }

    }

}