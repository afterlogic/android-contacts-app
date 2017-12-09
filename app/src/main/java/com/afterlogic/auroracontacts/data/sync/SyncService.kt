package com.afterlogic.auroracontacts.data.sync

import android.content.ContentResolver
import android.os.Bundle
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedException
import com.afterlogic.auroracontacts.presentation.background.calendarsSync.CalendarsSyncService
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

class SyncService @Inject constructor(
        res: Resources,
        private val accountService: AccountService
) {

    private val calendarAuthority = res.strings[R.string.calendar_authority]

    val isAnySyncRunning: Observable<Boolean> = CalendarsSyncService.serviceRunning

    fun requestSyncImmediately() : Completable {

        return accountService.account
                .firstOrError()
                .map { it.get() ?: throw UserNotAuthorizedException() }
                .doOnSuccess {

                    val settingsBundle = Bundle()
                    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                    settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)

                    ContentResolver.setIsSyncable(it, calendarAuthority, 1)

                    ContentResolver.requestSync(it, calendarAuthority, settingsBundle)

                }
                .toCompletable()

    }

}