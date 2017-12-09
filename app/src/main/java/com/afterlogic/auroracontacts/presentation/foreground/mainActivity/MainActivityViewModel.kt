package com.afterlogic.auroracontacts.presentation.foreground.mainActivity

import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.foreground.ActivityScope
import com.afterlogic.auroracontacts.presentation.navigation.AppRouter
import com.afterlogic.auroracontacts.presentation.navigation.Screens
import javax.inject.Inject

@ActivityScope
class MainActivityViewModel @Inject constructor(
        router: AppRouter,
        accountService: AccountService,
        subscriber: Subscriber
) : ObservableRxViewModel(subscriber) {

    init {

        accountService.accountSession
                .map { it.get() != null }
                .distinctUntilChanged()
                .retry()
                .defaultSchedulers()
                .subscribeIt {
                    router.newRootScreen(
                            if (it) Screens.MAIN
                            else Screens.LOGIN
                    )
                }

    }

}