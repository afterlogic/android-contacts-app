package com.afterlogic.auroracontacts.presentation.foreground.main

import com.afterlogic.auroracontacts.data.SyncPeriod
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.calendar.AuroraCalendarInfo
import com.afterlogic.auroracontacts.data.calendar.CalendarsRepository
import com.afterlogic.auroracontacts.data.contacts.ContactGroupInfo
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import com.afterlogic.auroracontacts.data.preferences.Prefs
import com.afterlogic.auroracontacts.data.sync.SyncRepository
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
        private val accountService: AccountService,
        private val calendarsRepository: CalendarsRepository,
        private val contactsRepository: ContactsRepository,
        private val prefs: Prefs,
        private val syncRepository: SyncRepository
) {

    val syncOnLocalChanges: Single<Boolean> get() = syncRepository.syncable

    fun setSyncOnLocalChanges(enabled: Boolean) : Completable =
            syncRepository.setSyncable(enabled)

    val syncPeriod: Single<SyncPeriod> get() = syncRepository.periodicallySyncPeriod
            .map { SyncPeriod.byDuration(it * 1000) ?: SyncPeriod.OFF }

    fun setSyncPeriod(period: SyncPeriod) : Completable = syncRepository.setPeriodicallySync(period.duration / 1000)

    fun getCalendars(): Flowable<List<AuroraCalendarInfo>> = calendarsRepository.getCalendarsInfo()

    fun getContactGroups(): Flowable<List<ContactGroupInfo>> = contactsRepository.getContactsGroupsInfo()

    fun setSyncEnabled(calendar: AuroraCalendarInfo, enabled: Boolean): Completable =
            calendarsRepository.setSyncEnabled(calendar, enabled)

    fun setSyncEnabled(contactGroupInfo: ContactGroupInfo, enabled: Boolean): Completable =
            contactsRepository.setSyncEnabled(contactGroupInfo, enabled)

    fun listenSyncingState(): Observable<Boolean> = syncRepository.isAnySyncRunning

    fun requestStartSyncImmediately() : Completable = syncRepository.requestSyncImmediately()

    fun obtainAccountName() : Observable<String> = accountService.account
            .filter { it.isNotNull }
            .map { it.get()!!.name }

    fun logout() : Completable = accountService.removeCurrentAccount()

}