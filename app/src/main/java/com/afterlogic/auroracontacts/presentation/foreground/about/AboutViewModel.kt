package com.afterlogic.auroracontacts.presentation.foreground.about

import android.databinding.Bindable
import com.afterlogic.auroracontacts.BuildConfig
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import com.afterlogic.auroracontacts.presentation.navigation.AppRouter
import com.afterlogic.auroracontacts.presentation.navigation.Screens
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

class AboutViewModel @Inject constructor(
        private val router: AppRouter,
        subscriber: Subscriber
): ObservableRxViewModel(subscriber) {

    @get:Bindable
    val versionName by bindable(BuildConfig.VERSION_NAME)

    @get:Bindable
    val versionCode by bindable(BuildConfig.VERSION_CODE)

    fun onLicencesClicked() {
        router.navigateTo(Screens.LICENCES)
    }

}