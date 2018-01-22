package com.afterlogic.auroracontacts.presentation.foreground.unsuportedApi

import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
class UnsupportedApiViewModel @Inject constructor(
        private val res: Resources,
        private val interactor: UnsupportedApiInteractor,
        subscriber: Subscriber
) : ObservableRxViewModel(subscriber) {

    var logoutButtonText by bindable(res.strings[R.string.action_logout])

    init {

        interactor.currentAccountName
                .defaultSchedulers()
                .subscribeIt {
                    logoutButtonText = res.strings[R.string.action_logout, it]
                }

    }

    fun onLogout() {

        interactor.logout()
                .defaultSchedulers()
                .subscribeIt()

    }

}