package com.afterlogic.auroracontacts.presentation.foreground.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.databinding.Bindable
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.core.rx.DisposableBag
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.rx.disposeBy
import com.afterlogic.auroracontacts.core.util.setSequentiallyFrom
import com.afterlogic.auroracontacts.data.SyncPeriod
import com.afterlogic.auroracontacts.data.calendar.AuroraCalendarInfo
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import com.afterlogic.auroracontacts.presentation.common.permissions.PermissionRequest
import com.afterlogic.auroracontacts.presentation.common.permissions.PermissionsInteractor
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
class MainViewModel @Inject constructor(
        private val interactor: MainInteractor,
        res: Resources,
        private val permissionsInteractor: PermissionsInteractor,
        subscriber: Subscriber
): ObservableRxViewModel(subscriber)  {

    companion object {

        private val TYPE_CALENDAR = 1

    }

    val cards: ObservableList<CardViewModel<Any>> = ObservableArrayList()

    @get:Bindable var loadingData by bindable(true)

    @get:Bindable var syncing by bindable(false)

    @get:Bindable var syncOnLocalChanges by bindable(false) {
        interactor.setSyncOnLocalChanges(it)
                .defaultSchedulers()
                .subscribeIt()
    }

    @get:Bindable var selectedSyncPeriod by bindable(0) {
        interactor.setSyncPeriod(SyncPeriod.values()[it])
                .defaultSchedulers()
                .subscribeIt()
    }

    private val calendarsMap = WeakHashMap<CalendarItemViewModel, AuroraCalendarInfo>()

    private val startedScopeDisposables = DisposableBag()

    private val calendars: MutableList<CalendarItemViewModel> = ObservableArrayList()
    private val calendarsCard = CardViewModel(
            TYPE_CALENDAR, res.strings[R.string.prompt_main_calendars_title], calendars
    )
    private val calendarItemStableIds = WeakHashMap<String, Long>()
    private var lastCaledarStableId = 0L

    init {

        interactor.getCalendars()
                .map { it.toTypedArray() }
                .distinctUntilChanged { f, s -> f contentEquals s }
                .defaultSchedulers()
                .subscribeIt(onNext = this::handle)

        interactor.syncPeriod
                .subscribeIt {
                    selectedSyncPeriod = it.ordinal
                }

        interactor.syncOnLocalChanges
                .subscribeIt {
                    syncOnLocalChanges = it
                }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onViewStart() {

        interactor.listenSyncingState()
                .retry()
                .disposeBy(startedScopeDisposables)
                .defaultSchedulers()
                .subscribeIt { syncing = it }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onViewStop() {
        startedScopeDisposables.dispose()
    }

    fun onSyncClicked() {

        if (syncing) return

        permissionsInteractor.requirePermission(PermissionRequest.CALENDAR)
                .subscribeIt()

        interactor.requestStartSyncImmediately()
                .defaultSchedulers()
                .subscribeIt()

    }

    private fun handle(calendars: Array<AuroraCalendarInfo>) {

        if (loadingData) loadingData = false

        calendars
                .map { mapCalendar(it) to it  }
                .also {

                    calendarsMap.clear()

                    if (it.isEmpty()) {

                        cards.remove(calendarsCard)

                    } else {

                        calendarsMap.putAll(it)

                        val calendarsVMs = it.map { (vm , _) -> vm }
                        this.calendars.setSequentiallyFrom(calendarsVMs)

                        if(!cards.contains(calendarsCard)) {
                            cards.add(calendarsCard)
                        }

                    }

                }

    }

    private fun mapCalendar(calendar: AuroraCalendarInfo) : CalendarItemViewModel =
            CalendarItemViewModel(
                    calendarItemStableIds.getOrPut(calendar.id) { ++lastCaledarStableId },
                    calendar.name, calendar.color,
                    calendar.settings.syncEnabled, this::onCheckedChanged
            )

    private fun onCheckedChanged(vm: CalendarItemViewModel, checked: Boolean) {

        val calendar = calendarsMap[vm] ?: return

        Timber.d("onChanged: ${calendar.name} : $checked")

        interactor.setSyncEnabled(calendar, checked)
                .defaultSchedulers()
                .subscribeIt()

    }

}