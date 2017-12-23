package com.afterlogic.auroracontacts.data.loginStateDataController

import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.rx.with
import com.afterlogic.auroracontacts.core.util.compareAndSet
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.data.preferences.Prefs
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Created by sunny on 10.12.2017.
 * mail: mail@sunnydaydev.me
 */

class LoginStateDataController @Inject constructor(
        private val accountService: AccountService,
        private val subscriber: Subscriber,
        private val calendarsDao: CalendarsDao,
        private val prefs: Prefs
) {

    private val started = AtomicBoolean(false)
    private var firstResult = AtomicBoolean(true)

    private var wasLoggedIn = false

    fun start() {

        if (!started.compareAndSet(true)) return

        accountService.account
                .doOnError(Timber::e)
                .retry()
                .observeOn(Schedulers.io())
                .with(subscriber)
                .subscribe {

                    val loggedIn = it.isNotNull

                    if (firstResult.getAndSet(false)) {
                        wasLoggedIn = loggedIn
                        return@subscribe
                    }

                    if (!loggedIn && wasLoggedIn) {
                        onLoggedOut()
                    }

                    wasLoggedIn = loggedIn

                }

    }

    private fun onLoggedOut() {

        Timber.d("Logged out. Delete all user depended data.")

        prefs.calendarsFetched = false
        prefs.contactsFetched = false
        prefs.syncOnLocalChanges = false
        prefs.syncPeriod = -1

        calendarsDao.deleteAll()

    }

}