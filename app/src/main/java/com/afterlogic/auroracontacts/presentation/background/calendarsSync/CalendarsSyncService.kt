package com.afterlogic.auroracontacts.presentation.background.calendarsSync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.IBinder
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.presentation.common.base.InjectionDaggerService
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CalendarsSyncService: InjectionDaggerService<Provider<CalendarSyncAdapter>>() {

    companion object {

        val serviceRunning = BehaviorSubject.createDefault(false)

        private lateinit var syncAdapter: CalendarSyncAdapter

        private object lock

    }

    private val adapterProvider by injectable { this }

    override fun onCreate() {
        super.onCreate()
        synchronized(lock) {
            syncAdapter = adapterProvider.get()
        }
        serviceRunning.onNext(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning.onNext(false)
    }

    override fun onBind(intent: Intent?): IBinder = syncAdapter.syncAdapterBinder

}

class CalendarSyncAdapter @Inject constructor(
        context: App
): AbstractThreadedSyncAdapter(context, true, false) {

    override fun onPerformSync(account: Account?,
                               params: Bundle?,
                               authority: String?,
                               contentProviderClient: ContentProviderClient?,
                               syncResult: SyncResult?) {

        Timber.d("onPerformSync")

        Thread.sleep(5000)

        Timber.d("onPerformSyncEnd")

    }

}