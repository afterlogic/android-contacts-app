package com.afterlogic.auroracontacts.presentation.background.calendarsSync

import android.accounts.Account
import android.app.Service
import android.content.*
import android.os.Bundle
import android.os.IBinder
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CalendarsSyncService: Service() {// InjectionDaggerService<Provider<CalendarSyncAdapter>>() {

    companion object {

        val serviceRunning = BehaviorSubject.createDefault(false)

        private lateinit var syncAdapter: CalendarSyncAdapter

        private object lock

    }

    //private val adapterProvider by injectable { this }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        synchronized(lock) {
            syncAdapter = CalendarSyncAdapter(this, true, false)
        }
        serviceRunning.onNext(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        serviceRunning.onNext(false)
    }

    override fun onBind(intent: Intent?): IBinder = syncAdapter.syncAdapterBinder

}

class CalendarSyncAdapter: AbstractThreadedSyncAdapter {

    constructor(context: Context, autoInitialize: Boolean): super(context, autoInitialize)

    constructor(context: Context, autoInitialize: Boolean, allowParallelSyncs: Boolean) : super(context, autoInitialize, allowParallelSyncs)

    override fun onPerformSync(account: Account?,
                               params: Bundle?,
                               authority: String?,
                               contentProviderClient: ContentProviderClient?,
                               syncResult: SyncResult?) {

        Timber.d("onPerformSync")

        Thread.sleep(10000)

        Timber.d("onPerformSyncEnd")

    }

}