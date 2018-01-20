package com.afterlogic.auroracontacts.application.wrappers

import android.support.annotation.StringRes
import com.afterlogic.auroracontacts.application.App
import javax.inject.Inject

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
class Resources @Inject constructor(private val context: App) {

    val strings = Strings()

    inner class Strings {

        operator fun get(@StringRes id: Int): String {
            return context.getString(id)
        }

        operator fun get(@StringRes id: Int, vararg args: Any?): String {
            return context.getString(id, *args)
        }

    }

}