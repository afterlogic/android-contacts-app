package com.afterlogic.auroracontacts.presentation.foreground.mainActivity

import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.util.Optional
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.account.AuroraSession
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.foreground.ActivityScope
import com.afterlogic.auroracontacts.presentation.navigation.AppRouter
import com.afterlogic.auroracontacts.presentation.navigation.Screens
import io.reactivex.functions.Function
import javax.inject.Inject

@ActivityScope
class MainActivityViewModel @Inject constructor(
        router: AppRouter,
        accountService: AccountService,
        subscriber: Subscriber
) : ObservableRxViewModel(subscriber) {

    private var currentScreen: String? = null

    init {

        accountService.accountSession
                .distinctUntilChanged(Function<Optional<AuroraSession>, AuroraSession?> { it.get() })
                .retry()
                .defaultSchedulers()
                .subscribeIt {

                    val session = it.get()

                    val screen = when {
                        session == null -> Screens.LOGIN
                        !supportedApi(session.apiVersion) -> Screens.UNSUPPORTED_API
                        else -> Screens.MAIN
                    }

                    if (screen == currentScreen) {
                        return@subscribeIt
                    }

                    router.newRootScreen(screen)

                }

    }

    private fun supportedApi(version: Int): Boolean {

        return ApiType.supportedApiTypes.any { it.code == version }

    }

}