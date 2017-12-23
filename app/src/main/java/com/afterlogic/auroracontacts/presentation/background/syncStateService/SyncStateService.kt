package com.afterlogic.auroracontacts.presentation.background.syncStateService

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.core.rx.MultiProcessObservable
import com.afterlogic.auroracontacts.core.rx.ObservableMessenger
import com.afterlogic.auroracontacts.core.util.IntentUtil
import com.afterlogic.auroracontacts.presentation.common.base.InjectionDaggerService
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by sunny on 10.12.2017.
 * mail: mail@sunnydaydev.me
 */
class SyncStateService: InjectionDaggerService<SyncStateService.SyncStateObservableMessenger>() {

    companion object {

        fun bindIntent(): Intent = IntentUtil.intent<SyncStateService>()

        private val SYNC_STATE_REQUEST = 1

    }

    private val messenger by inject { it }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
    }

    override fun onBind(intent: Intent): IBinder = messenger.binder

    class Connections @Inject constructor(private val context: App) {

        val anySync: Observable<Boolean> get() = MultiProcessObservable(
                context, bindIntent(), SYNC_STATE_REQUEST, true
        ) { it.getBoolean("sync") }

    }

    class SyncStateObservableMessenger @Inject constructor(
            private val syncStateHolder: SyncStateHolder
    ) : ObservableMessenger(Looper.getMainLooper()) {

        override fun getObservable(requestCode: Int): Observable<Bundle> {
            return syncStateHolder.anySync.map {
                Bundle().apply { putBoolean("sync", it) }
            }
        }

    }

}

@AppScope
class SyncStateHolder @Inject constructor() {

    val calendarsSyncing: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val contactsSyncing: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val anySync = Observables.combineLatest(calendarsSyncing, contactsSyncing) { f, s -> f || s  }

}