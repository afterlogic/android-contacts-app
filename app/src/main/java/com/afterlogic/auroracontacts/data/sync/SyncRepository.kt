package com.afterlogic.auroracontacts.data.sync

import android.accounts.Account
import android.content.ContentResolver
import android.os.Bundle
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.core.util.rem
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedError
import com.afterlogic.auroracontacts.data.preferences.Prefs
import com.afterlogic.auroracontacts.presentation.background.syncStateService.SyncStateService
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class SyncRepository @Inject constructor(
        res: Resources,
        private val accountService: AccountService,
        private val syncStateConnections: SyncStateService.Connections,
        private val prefs: Prefs
) {

    companion object {
        private const val KEY_TYPE = "com.afterlogic.aurora.syncType"
        private const val TYPE_PERIODIC = 1
    }

    private val calendarAuthority = res.strings[R.string.calendar_authority]
    private val contactsAuthority = res.strings[R.string.contacts_authority]

    private val periodicSyncBundle get() = Bundle().apply {
        putInt(KEY_TYPE, TYPE_PERIODIC)
    }

    val isAnySyncRunning: Observable<Boolean> get() = syncStateConnections.anySync

    fun requestSyncImmediately() : Completable = completableByAccount {

        val settingsBundle = Bundle()
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)

        ContentResolver.requestSync(it, calendarAuthority, settingsBundle)
        ContentResolver.requestSync(it, contactsAuthority, settingsBundle)

        Timber.d("requestSyncImmediately")

    }

    val periodicallySyncPeriod: Single<Long> get() = singleByAccount {
        prefs.automaticallySyncPeriod
    }

    val syncOnChanges: Single<Boolean> get() = singleByAccount {
        prefs.automaticallySyncOnChanges
    }

    fun setPeriodicallySync(intervalInSeconds: Long) : Completable = completableByAccount {

        prefs.automaticallySyncPeriod = intervalInSeconds

        checkAndSetIsSyncable(it)

        if (intervalInSeconds > 0) {

            val bundle = periodicSyncBundle

            ContentResolver.addPeriodicSync(it, contactsAuthority, bundle, intervalInSeconds)
            ContentResolver.addPeriodicSync(it, calendarAuthority, bundle, intervalInSeconds)

        }

    }

    fun setSyncOnChanges(syncable: Boolean) : Completable = completableByAccount {

        prefs.automaticallySyncOnChanges = syncable

        checkAndSetIsSyncable(it)

    }

    fun evaluateSyncSettings() : Completable = Completable.defer {

        Completable.concat(listOf(
                setSyncOnChanges(prefs.automaticallySyncOnChanges).onErrorComplete(),
                setPeriodicallySync(prefs.automaticallySyncPeriod).onErrorComplete()
        ))

    }

    fun isPeriodicallySync(requestBundle: Bundle) : Boolean =
            requestBundle.getInt(KEY_TYPE) == TYPE_PERIODIC

    private fun checkAndSetIsSyncable(account: Account) {

        val syncable = prefs.automaticallySyncOnChanges || prefs.automaticallySyncPeriod > 0L

        ContentResolver.setIsSyncable(account, contactsAuthority, syncable % { 1 } ?: 0)
        ContentResolver.setIsSyncable(account, calendarAuthority, syncable % { 1 } ?: 0)

        ContentResolver.setSyncAutomatically(account, contactsAuthority, syncable)
        ContentResolver.setSyncAutomatically(account, calendarAuthority, syncable)

        if (!syncable) {

            val periodicBundle = periodicSyncBundle

            ContentResolver.removePeriodicSync(account, contactsAuthority, periodicBundle)
            ContentResolver.removePeriodicSync(account, calendarAuthority, periodicBundle)

        }

    }

    private fun completableByAccount(action: (Account) -> Unit) : Completable {
        return accountService.account
                .firstOrError()
                .map { it.get() ?: throw UserNotAuthorizedError() }
                .doOnSuccess(action)
                .toCompletable()
    }

    private fun <T> singleByAccount(action: (Account) -> T) : Single<T> {
        return accountService.account
                .firstOrError()
                .map { it.get() ?: throw UserNotAuthorizedError() }
                .map(action)
    }

}