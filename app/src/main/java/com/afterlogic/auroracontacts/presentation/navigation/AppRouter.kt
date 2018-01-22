package com.afterlogic.auroracontacts.presentation.navigation

import android.content.Context
import android.content.Intent
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.presentation.common.FragmentCreator
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.SupportAppNavigator
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

object Screens {

    const val LOGIN = "login"
    const val MAIN = "main"
    const val UNSUPPORTED_API = "unsupported_api"
    const val ABOUT = "about"
    const val LICENCES = "licenses"
    const val LICENCE = "licence"

}

class AppRouter : Router()

class AppNavigator private constructor(
        activity: FragmentActivity,
        private val fragmentProviders: Map<String, FragmentCreator>,
        @IdRes contentId: Int = R.id.content
): SupportAppNavigator(activity, contentId) {

    class Factory @Inject constructor(
            private val fragments: Map<String, @JvmSuppressWildcards FragmentCreator>
    ) {

        fun create(activity: FragmentActivity, @IdRes contentId: Int = R.id.content): AppNavigator =
                AppNavigator(activity, fragments, contentId)

    }

    override fun createActivityIntent(context: Context, screenKey: String, data: Any?): Intent? = null

    override fun createFragment(screenKey: String, data: Any?): Fragment? = fragmentProviders[screenKey]?.create(data)

}