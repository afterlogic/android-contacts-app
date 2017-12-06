package com.afterlogic.auroracontacts.presentation.foreground.main

import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.presentation.ActivityScope
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class MainViewModel @Inject constructor(
        accountService: AccountService,
        subscriber: Subscriber
) : ObservableRxViewModel(subscriber) {

    init {

        accountService.currentAccountSession
                .defaultSchedulers()
                .subscribeIt {
                    Timber.d("Session: ${it.get() ?: "null"}")
                }

    }

}