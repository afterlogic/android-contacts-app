package com.afterlogic.auroracontacts.presentation.foreground.main

import com.afterlogic.auroracontacts.data.SyncPeriod
import com.afterlogic.auroracontacts.data.calendar.AuroraCalendar
import com.afterlogic.auroracontacts.data.calendar.CalendarsRepository
import com.afterlogic.auroracontacts.data.preferences.Prefs
import com.afterlogic.auroracontacts.data.sync.SyncService
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class MainInteractor @Inject constructor(
        private val calendarsRepository: CalendarsRepository,
        private val prefs: Prefs,
        private val syncService: SyncService
) {

    val syncOnLocalChanges: Single<Boolean> get() = Single.fromCallable { prefs.syncOnLocalChanges }

    fun setSyncOnLocalChanges(enabled: Boolean) : Completable = Completable.fromAction {
        prefs.syncOnLocalChanges = enabled
    }

    val syncPeriod: Single<SyncPeriod> get() = Single.fromCallable {
        SyncPeriod.byDuration(prefs.syncPeriod) ?: SyncPeriod.OFF
    }

    fun setSyncPeriod(period: SyncPeriod) : Completable = Completable.fromAction {
        prefs.syncPeriod = period.duration
    }

    fun getCalendars(): Flowable<List<AuroraCalendar>> = calendarsRepository.getCalendars()

    fun setSyncEnabled(calendar: AuroraCalendar, enabled: Boolean): Completable =
            calendarsRepository.setSyncEnabled(calendar, enabled)

    fun listenSyncingState(): Observable<Boolean> = syncService.isAnySyncRunning

    fun requestStartSyncImmediately() : Completable = syncService.requestSyncImmediately()

}