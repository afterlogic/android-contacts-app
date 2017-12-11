package com.afterlogic.auroracontacts.presentation.background.sync.calendar

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentValues
import android.net.Uri
import android.provider.CalendarContract
import com.afterlogic.auroracontacts.data.calendar.CalendarMapper
import com.afterlogic.auroracontacts.data.calendar.CalendarRemoteServiceProvider
import com.afterlogic.auroracontacts.data.calendar.RemoteCalendar
import com.afterlogic.auroracontacts.data.calendar.RemoteCalendarEvent
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.presentation.background.sync.Contract
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import io.reactivex.Completable
import io.reactivex.rxkotlin.Singles
import java.util.*
import javax.inject.Inject

class CalendarSyncOperation private constructor(
        private val account: Account,
        private val contentClient: ContentProviderClient,
        private val dao: CalendarsDao,
        private val calendarMapper: CalendarMapper,
        private val remoteServiceProvider: CalendarRemoteServiceProvider
) {

    class Factory @Inject constructor(
            private val dao: CalendarsDao,
            private val calendarMapper: CalendarMapper,
            private val remoteServiceProvider: CalendarRemoteServiceProvider
    ) {

        fun create(account: Account, client: ContentProviderClient) : CalendarSyncOperation {
            return CalendarSyncOperation(account, client, dao, calendarMapper, remoteServiceProvider)
        }

    }

    private val remoteService  = remoteServiceProvider.get()

    fun sync() : Completable = Completable.defer {

        val remoteCalendars = remoteService.flatMap { it.getCalendars() }

        val local = dao.all.map { it.map { calendarMapper.toPlain(it) } }.firstOrError()

        Singles.zip(remoteCalendars, local) { rem, loc ->
            val syncable = loc.filter { it.settings.syncEnabled }.map { it.id }
            rem.filter { syncable.contains(it.id) }
        }
                .flatMapCompletable(this::syncFromRemote)

    }

    fun syncFromRemote(calendars: List<RemoteCalendar>): Completable = Completable.defer {

        val calendarUri = getSyncUri(CalendarContract.Calendars.CONTENT_URI)

        // Delete unsyncable calendars
        val queryIds = calendars.joinToString("', '", "('", "')") { it.id }
        contentClient.delete(calendarUri, "${Contract.Calendar.REMOTE_ID} NOT IN $queryIds", null)

        // Check changed
        val cursor = contentClient.query(
                calendarUri,
                arrayOf(Contract.Calendar.REMOTE_ID, Contract.Calendar.REMOTE_CTAG),
                "1",
                null,
                null) ?: throw UnexpectedNullCursorException()

        val storedCTags = mutableMapOf<String, String>()

        while (cursor.moveToNext()) {
            storedCTags[cursor.getString(0)] = cursor.getString(1)
        }

        cursor.close()

        val toSync = calendars
                        .filterNot { it.cTag == storedCTags[it.id] }

        // Update and insert
        toSync.map { it.id to it.toContentValues() }
                // Try update exists
                .filter { (id, cv) ->

                    contentClient.update(
                            calendarUri, cv,
                            "${Contract.Calendar.REMOTE_ID} = ?", arrayOf(id)
                    ) == 0

                }
                // Insert new
                .forEach { (_, cv) ->
                    contentClient.insert(calendarUri, cv)
                }

        toSync.map {
            syncEventsFromRemote(it)
                    .doOnComplete {

                        val ctag = ContentValues()
                        ctag.put(Contract.Calendar.REMOTE_CTAG, it.cTag)

                        contentClient.update(
                                calendarUri,
                                ctag,
                                "${Contract.Calendar.REMOTE_ID} = ?",
                                arrayOf(it.id)
                        )

                    }
        }
                .let { Completable.concat(it) }
    }

    private fun syncEventsFromRemote(calendar: RemoteCalendar): Completable {

        return remoteService.flatMap { it.getEvents(calendar.id) }
                .doOnSuccess(this::storeEvents)
                .toCompletable()

    }

    private fun storeEvents(events: List<RemoteCalendarEvent>) {



    }


    private fun getSyncUri(uri: Uri): Uri {
        return uri.buildUpon()
                .appendQueryParameter("caller_is_syncadapter", "true")
                .appendQueryParameter("account_name", account.name)
                .appendQueryParameter("account_type", account.type)
                .build()
    }

    // TODO: Timezone
    private fun RemoteCalendar.toContentValues(
            localId: Long? = null,
            timeZone: TimeZone = TimeZone.getDefault()
    ): ContentValues {

        val cv = ContentValues()

        if (localId == null) {
            cv.put(CalendarContract.Calendars.DIRTY, 0)
        } else {
            cv.put(CalendarContract.Calendars._ID, localId)
        }

        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
        cv.put(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, this.name)
        cv.put(CalendarContract.Calendars.CALENDAR_COLOR, this.color)
        cv.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.id)
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, this.owner)
        cv.put(Contract.Calendar.REMOTE_ID, this.id)
        //cv.put(Contract.Calendar.REMOTE_CTAG, this.cTag)
        cv.put(
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                if (this.accessLevel == RemoteCalendar.AccessLevel.EDITOR)
                    CalendarContract.Calendars.CAL_ACCESS_EDITOR
                else CalendarContract.Calendars.CAL_ACCESS_READ
        )
        cv.put(CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES, 0)
        cv.put(CalendarContract.Calendars.ALLOWED_AVAILABILITY, 0)

        return cv

    }

}