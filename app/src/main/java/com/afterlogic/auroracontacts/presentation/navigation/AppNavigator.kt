package com.afterlogic.auroracontacts.presentation.navigation

import android.content.Intent
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginFragment
import com.afterlogic.auroracontacts.presentation.foreground.main.MainFragment
import ru.terrakok.cicerone.android.SupportAppNavigator
import javax.inject.Inject

/**
 * Created by sunny on 07.12.2017.
 * mail: mail@sunnydaydev.me
 */

class AppNavigator @Inject constructor(
        activity: FragmentActivity, @IdRes contentId: Int = R.id.content
): SupportAppNavigator(activity, contentId) {

    override fun createActivityIntent(screenKey: String, data: Any?): Intent? = null

    override fun createFragment(screenKey: String, data: Any?): Fragment? = when(screenKey) {

        AppRouter.LOGIN -> LoginFragment.instance()
        AppRouter.MAIN -> MainFragment.instance()

        else -> null

    }

}