package com.afterlogic.auroracontacts.presentation.background.sync.calendar

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.IBinder
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.presentation.background.syncStateService.SyncStateHolder
import com.afterlogic.auroracontacts.presentation.common.base.InjectionDaggerService
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

class CalendarsSyncService: InjectionDaggerService<CalendarsSyncInjection>() {

    private val syncAdapter by inject { it.adapter }
    private val stateHolder by inject { it.syncStateHolder }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        stateHolder.calendarsSyncing.onNext(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        stateHolder.calendarsSyncing.onNext(false)
    }

    override fun onBind(intent: Intent?): IBinder = syncAdapter.syncAdapterBinder

}

class CalendarsSyncInjection @Inject constructor(
        val adapter: CalendarSyncAdapter,
        val syncStateHolder: SyncStateHolder
)

@AppScope
class CalendarSyncAdapter @Inject constructor(
        context: App,
        private val calendarsSyncOperation: CalendarsSyncOperation.Factory
): AbstractThreadedSyncAdapter(context, true, false) {

    override fun onPerformSync(account: Account,
                               params: Bundle?,
                               authority: String,
                               contentProviderClient: ContentProviderClient,
                               syncResult: SyncResult?) {

        Timber.d("onPerformSync")

        calendarsSyncOperation.create(account, contentProviderClient)
                .sync()
                .blockingGet()
                ?.let(Timber::e)

        Timber.d("onPerformSyncEnd")

    }



}

