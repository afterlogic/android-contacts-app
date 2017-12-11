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
        private val res: Resources,
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

    init {

        interactor.getCalendars()
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

    private fun handle(calendars: List<AuroraCalendarInfo>) {

        if (loadingData) loadingData = false

        calendars
                .map { mapCalendar(it) to it  }
                .also {

                    calendarsMap.putAll(it)

                    cards.clear()

                    val card = CardViewModel(
                            TYPE_CALENDAR,
                            res.strings[R.string.prompt_main_calendars_title],
                            it.map { (vm , _) -> vm }
                    )

                    cards.add(card)

                }

    }

    private fun mapCalendar(calendar: AuroraCalendarInfo) : CalendarItemViewModel =
            CalendarItemViewModel(
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