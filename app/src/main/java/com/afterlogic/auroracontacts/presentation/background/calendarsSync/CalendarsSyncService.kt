package com.afterlogic.auroracontacts.presentation.background.calendarsSync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.IBinder
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.data.calendar.CalendarsRepository
import com.afterlogic.auroracontacts.presentation.common.base.InjectionDaggerService
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CalendarsSyncService: InjectionDaggerService<CalendarSyncAdapter>() {

    private val adapter by injectable { this }

    override fun onBind(p0: Intent?): IBinder = adapter.syncAdapterBinder

}

class CalendarSyncAdapter @Inject constructor(
        private val repository: CalendarsRepository,
        context: App
): AbstractThreadedSyncAdapter(context, true) {

    override fun onPerformSync(account: Account,
                               params: Bundle,
                               authority: String,
                               contentProviderClient: ContentProviderClient,
                               syncResult: SyncResult) {

        Timber.d("onPerformSync")

    }

}