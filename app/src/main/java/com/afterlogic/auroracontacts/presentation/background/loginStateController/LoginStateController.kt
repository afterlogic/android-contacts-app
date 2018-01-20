package com.afterlogic.auroracontacts.presentation.background.loginStateController

import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.rx.with
import com.afterlogic.auroracontacts.core.util.compareAndSet
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.data.db.ContactsDao
import com.afterlogic.auroracontacts.data.preferences.Prefs
import com.afterlogic.auroracontacts.data.sync.SyncRepository
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Created by sunny on 10.12.2017.
 * mail: mail@sunnydaydev.me
 */

class LoginStateController @Inject constructor(
        private val accountService: AccountService,
        private val syncRepository: SyncRepository,
        private val subscriber: Subscriber,
        private val calendarsDao: CalendarsDao,
        private val contactsDao: ContactsDao,
        private val prefs: Prefs
) {

    private val started = AtomicBoolean(false)
    private var firstResult = AtomicBoolean(true)

    private var wasLoggedIn
        get() = prefs.loggedIn
        set(value) { prefs.loggedIn = value }

    fun start() {

        if (!started.compareAndSet(true)) return

        accountService.account
                .doOnError(Timber::e)
                .map { it.isNotNull }
                .flatMap {

                    if (it && !wasLoggedIn) {
                        syncRepository.setSyncable(true)
                                .onErrorComplete()
                                .andThen(Observable.just(it))
                    } else {
                        Observable.just(it)
                    }

                }
                .observeOn(Schedulers.io())
                .doOnNext { loggedIn ->

                    if (firstResult.getAndSet(false)) {
                        wasLoggedIn = loggedIn
                        return@doOnNext
                    }

                    if (!loggedIn && wasLoggedIn) {
                        onLoggedOut()
                    }

                    wasLoggedIn = loggedIn
                }
                .retry()
                .with(subscriber)
                .subscribe()

    }

    private fun onLoggedOut() {

        Timber.d("Logged out. Delete all user depended data.")

        prefs.calendarsFetched = false
        prefs.contactsFetched = false

        calendarsDao.deleteAll()
        contactsDao.deleteAll()

    }

}