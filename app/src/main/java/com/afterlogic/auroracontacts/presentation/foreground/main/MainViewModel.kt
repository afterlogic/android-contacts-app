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
import com.afterlogic.auroracontacts.data.calendar.AuroraCalendar
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
class MainViewModel @Inject constructor(
        private val interactor: MainInteractor,
        private val res: Resources,
        subscriber: Subscriber
): ObservableRxViewModel(subscriber)  {

    companion object {

        private val TYPE_CALENDAR = 1

    }

    val cards: ObservableList<CardViewModel> = ObservableArrayList()

    @get:Bindable var loadingData by bindable(true)

    @get:Bindable var syncing by bindable(false)

    private val calendarsMap = mutableMapOf<AuroraCalendar, CalendarItemViewModel>()

    private val startScopedDisposables = DisposableBag()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {

        interactor.getCalendars()
                .defaultSchedulers()
                .doOnSuccess { loadingData = false }
                .disposeBy(startScopedDisposables)
                .subscribeIt(onSuccess = this::handle)

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        startScopedDisposables.dispose()
    }

    fun onStartSyncClicked() {

    }

    private fun handle(calendars: List<AuroraCalendar>) {

        calendars
                .map { it to mapCalendar(it) }
                .also {

                    calendarsMap.clear()
                    calendarsMap.putAll(it)

                    cards.clear()

                    val card = CardViewModel(
                            TYPE_CALENDAR,
                            res.strings[R.string.prompt_main_calendars_title],
                            it.map { (_ , vm) -> vm }
                    )

                    cards.add(card)

                }

    }

    private fun onCheckedChanged(calendar: AuroraCalendar, checked: Boolean) {
        Timber.d("onChanged: ${calendar.name} : $checked")
    }

    private fun mapCalendar(calendar: AuroraCalendar) : CalendarItemViewModel =
            CalendarItemViewModel(calendar.name, calendar.color, true) {
                onCheckedChanged(calendar, it)
            }

}