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
import com.afterlogic.auroracontacts.core.util.StringIdProvider
import com.afterlogic.auroracontacts.core.util.setSequentiallyFrom
import com.afterlogic.auroracontacts.data.SyncPeriod
import com.afterlogic.auroracontacts.data.calendar.AuroraCalendarInfo
import com.afterlogic.auroracontacts.data.contacts.ContactGroupInfo
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import com.afterlogic.auroracontacts.presentation.common.permissions.PermissionRequest
import com.afterlogic.auroracontacts.presentation.common.permissions.PermissionsInteractor
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

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

        private const val TYPE_CALENDAR = 1
        private const val TYPE_CONTACTS = 2

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
    private val contactsMap = WeakHashMap<ContactItemViewModel, ContactGroupInfo>()

    private val startedScopeDisposables = DisposableBag()

    private val calendars: MutableList<CalendarItemViewModel> = ObservableArrayList()
    private val calendarsCard = CardViewModel(
            TYPE_CALENDAR, res.strings[R.string.prompt_main_calendars_title], calendars
    )
    private val calendarItemIdProvider = StringIdProvider()

    private val contacts: MutableList<ContactItemViewModel> = ObservableArrayList()
    private val contactsCard = CardViewModel(
            TYPE_CONTACTS, res.strings[R.string.prompt_main_contacts_title], contacts
    )

    private var calendarsLoading by Delegates.observable(true) { _, _, _ ->
        updateLoadingStatus()
    }

    private var contactsLoading by Delegates.observable(true) { _, _, _ ->
        updateLoadingStatus()
    }


    init {

        interactor.getCalendars()
                .map { it.toTypedArray() }
                .distinctUntilChanged { f, s -> f contentEquals s }
                .defaultSchedulers()
                .subscribeIt(onNext = this::handleCalendars)

        interactor.getContactGroups()
                .map { it.toTypedArray() }
                .distinctUntilChanged { f, s -> f contentEquals s }
                .defaultSchedulers()
                .subscribeIt(onNext = this::handleContacts)

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

        permissionsInteractor.requirePermission(PermissionRequest.CALENDAR_AND_CONTACTS)
                .subscribeIt()

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onViewStop() {
        startedScopeDisposables.dispose()
    }

    fun onSyncClicked() {

        if (syncing) return

        interactor.requestStartSyncImmediately()
                .defaultSchedulers()
                .subscribeIt()

    }

    private fun handleCalendars(calendars: Array<AuroraCalendarInfo>) {

        calendarsLoading = false

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

    private fun handleContacts(contacts: Array<ContactGroupInfo>) {

        contactsLoading = false

        // Check exists
        val sameContent by lazy {
            val currents = contactsMap.values
            if (currents.size != contacts.size) return@lazy false
            val currentIds = currents.map { it.id }.toTypedArray()
            val fetchedIds = contacts.map { it.id }.toTypedArray()
            currentIds contentEquals fetchedIds
        }

        if (sameContent) {

            contactsMap.forEach { (key, value) ->

                val contact = contacts.find { it.id == value.id } ?: throw error("Contact not exists.")

                key.name = contact.name
                key.checked = contact.syncing

            }

            return

        }

        contacts
                .map { mapContactsGroup(it) to it  }
                .also {

                    contactsMap.clear()

                    if (it.isEmpty()) {

                        cards.remove(contactsCard)

                    } else {

                        contactsMap.putAll(it)

                        val contactsVMs = it.map { (vm , _) -> vm }
                        this.contacts.setSequentiallyFrom(contactsVMs)

                        if(!cards.contains(contactsCard)) {
                            cards.add(0, contactsCard)
                        }

                    }

                }

    }

    private fun mapCalendar(calendar: AuroraCalendarInfo) : CalendarItemViewModel =
            CalendarItemViewModel(
                    calendarItemIdProvider[calendar.id],
                    calendar.name, calendar.color,
                    calendar.settings.syncEnabled, this::onCalendarCheckedChanged
            )

    private fun mapContactsGroup(group: ContactGroupInfo) : ContactItemViewModel =
            ContactItemViewModel(
                    group.id, group.name, group.syncing, this::onContactGroupCheckedChanged
            )

    private fun onCalendarCheckedChanged(vm: CalendarItemViewModel, checked: Boolean) {

        val calendar = calendarsMap[vm] ?: return

        Timber.d("onChanged: ${calendar.name} : $checked")

        interactor.setSyncEnabled(calendar, checked)
                .defaultSchedulers()
                .subscribeIt()

    }

    private fun onContactGroupCheckedChanged(vm: ContactItemViewModel, checked: Boolean) {

        val calendar = contactsMap[vm] ?: return

        Timber.d("onChanged: ${calendar.name} : $checked")

        interactor.setSyncEnabled(calendar, checked)
                .defaultSchedulers()
                .subscribeIt()

    }

    private fun updateLoadingStatus() {
        val loading = calendarsLoading || contactsLoading
        if (loading != this.loadingData) loadingData = loading
    }

}