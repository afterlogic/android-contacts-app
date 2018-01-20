package com.afterlogic.auroracontacts.data.sync

import android.accounts.Account
import android.content.ContentResolver
import android.os.Bundle
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.core.util.rem
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedException
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

class SyncRepository @Inject constructor(
        res: Resources,
        private val accountService: AccountService,
        private val syncStateConnections: SyncStateService.Connections
) {

    private val calendarAuthority = res.strings[R.string.calendar_authority]
    private val contactsAuthority = res.strings[R.string.contacts_authority]

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
        ContentResolver.getPeriodicSyncs(it, contactsAuthority)
                .firstOrNull()?.period ?: -1L
    }

    val syncable: Single<Boolean> get() = singleByAccount {
        ContentResolver.getSyncAutomatically(it, contactsAuthority)
    }

    fun setPeriodicallySync(intervalInSeconds: Long) : Completable = completableByAccount {

        if (!ContentResolver.getSyncAutomatically(it, contactsAuthority) ||
                !ContentResolver.getSyncAutomatically(it, calendarAuthority)) {

            throw IllegalStateException("Sync not enabled.")

        }

        if (intervalInSeconds > 0) {

            ContentResolver.addPeriodicSync(it, contactsAuthority, Bundle.EMPTY, intervalInSeconds)
            ContentResolver.addPeriodicSync(it, calendarAuthority, Bundle.EMPTY, intervalInSeconds)

        } else {

            ContentResolver.removePeriodicSync(it, contactsAuthority, Bundle.EMPTY)
            ContentResolver.removePeriodicSync(it, calendarAuthority, Bundle.EMPTY)

        }

    }

    fun setSyncable(syncable: Boolean) : Completable = completableByAccount {

        ContentResolver.setIsSyncable(it, contactsAuthority, syncable % { 1 } ?: 0)
        ContentResolver.setIsSyncable(it, calendarAuthority, syncable % { 1 } ?: 0)

        ContentResolver.setSyncAutomatically(it, contactsAuthority, syncable)
        ContentResolver.setSyncAutomatically(it, calendarAuthority, syncable)

        if (!syncable) {

            ContentResolver.removePeriodicSync(it, contactsAuthority, Bundle.EMPTY)
            ContentResolver.removePeriodicSync(it, calendarAuthority, Bundle.EMPTY)

        }

    }

    private fun completableByAccount(action: (Account) -> Unit) : Completable {
        return accountService.account
                .firstOrError()
                .map { it.get() ?: throw UserNotAuthorizedException() }
                .doOnSuccess(action)
                .toCompletable()
    }

    private fun <T> singleByAccount(action: (Account) -> T) : Single<T> {
        return accountService.account
                .firstOrError()
                .map { it.get() ?: throw UserNotAuthorizedException() }
                .map(action)
    }

}