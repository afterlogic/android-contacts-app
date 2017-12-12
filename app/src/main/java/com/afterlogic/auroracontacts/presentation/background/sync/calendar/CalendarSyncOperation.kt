package com.afterlogic.auroracontacts.presentation.background.sync.calendar

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.ContentValues
import android.net.Uri
import android.provider.CalendarContract
import biweekly.Biweekly
import biweekly.component.VEvent
import biweekly.util.Recurrence
import com.afterlogic.auroracontacts.data.calendar.CalendarMapper
import com.afterlogic.auroracontacts.data.calendar.CalendarRemoteServiceProvider
import com.afterlogic.auroracontacts.data.calendar.RemoteCalendar
import com.afterlogic.auroracontacts.data.calendar.RemoteCalendarEvent
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.presentation.background.sync.Contract
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import io.reactivex.Completable
import io.reactivex.rxkotlin.Singles
import java.io.IOException
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CalendarSyncOperation private constructor(
        private val account: Account,
        private val contentClient: ContentProviderClient,
        private val dao: CalendarsDao,
        private val calendarMapper: CalendarMapper,
        remoteServiceProvider: CalendarRemoteServiceProvider
) {

    companion object {

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

    @SuppressLint("Recycle")
    fun syncFromRemote(calendars: List<RemoteCalendar>): Completable = Completable.defer {

        val calendarUri = getSyncUri(CalendarContract.Calendars.CONTENT_URI)

        // Delete unsyncable calendars
        val queryIds = calendars.joinToString("', '", "('", "')") { it.id }
        contentClient.delete(calendarUri, "${Contract.Calendar.REMOTE_ID} NOT IN $queryIds", null)

        val cursor = contentClient.query(
                calendarUri,
                arrayOf(
                        CalendarContract.Calendars._ID,
                        Contract.Calendar.REMOTE_ID,
                        Contract.Calendar.REMOTE_CTAG
                ),
                "1",
                null,
                null) ?: throw UnexpectedNullCursorException()

        val cTags = mutableMapOf<String, String>()
        val localIds = mutableMapOf<String, Long>()

        while (cursor.moveToNext()) {

            val id = cursor.getString(1)

            localIds[id] = cursor.getLong(0)

            if (!cursor.isNull(2)) {
                cTags[id] = cursor.getString(2)
            }

        }

        cursor.close()

        calendars
                .filterNot { it.cTag == cTags[it.id] }
                .also {
                    it.forEach {

                        val localId = localIds[it.id]

                        val cv = it.toContentValues()

                        if (localId != null) {

                            contentClient.update(
                                    calendarUri,
                                    cv,
                                    "${CalendarContract.Calendars._ID} = $localId",
                                    null
                            )

                        } else {

                            val id = contentClient.insert(calendarUri, cv).lastPathSegment.toLong()
                            localIds[it.id] = id

                        }

                    }
                }
                .map {

                    syncEventsFromRemote(it, localIds[it.id]!!)
                            .doOnComplete {

                                val cTag = ContentValues()
                                cTag.put(Contract.Calendar.REMOTE_CTAG, it.cTag)

                                contentClient.update(
                                        calendarUri,
                                        cTag,
                                        "${Contract.Calendar.REMOTE_ID} = ?",
                                        arrayOf(it.id)
                                )

                            }

                }
                .let { Completable.concat(it) }

    }

    private fun syncEventsFromRemote(calendar: RemoteCalendar, localId: Long): Completable {

        return remoteService.flatMap { it.getEvents(calendar.id) }
                .doOnSuccess { storeEvents(localId, it) }
                .toCompletable()

    }

    @SuppressLint("Recycle")
    private fun storeEvents(localCalendarId: Long, events: List<RemoteCalendarEvent>) {

        val eventsUri = getSyncUri(CalendarContract.Events.CONTENT_URI)

        val idsQuery = events.joinToString("', '", "('", "')") { it.id }

        // Delete unexists
        contentClient.delete(
                eventsUri,
                "${CalendarContract.Events.CALENDAR_ID} = $localCalendarId AND ${Contract.Calendar.REMOTE_ID} NOT IN $idsQuery",
                null

        )

        val cursor = contentClient.query(
                eventsUri,
                arrayOf(
                        CalendarContract.Events._ID,
                        Contract.Events.REMOTE_ID,
                        Contract.Events.REMOTE_ETAG
                ),
                "${CalendarContract.Events.CALENDAR_ID} = ?",
                arrayOf(localCalendarId.toString()),
                null
        ) ?: throw UnexpectedNullCursorException()

        val eTags = mutableMapOf<String, String>()
        val localIds = mutableMapOf<String, Long>()

        while (cursor.moveToNext()) {

            val remoteId = cursor.getString(1)

            localIds[remoteId] = cursor.getLong(0)

            if (!cursor.isNull(2)) {
                eTags[remoteId] = cursor.getString(2)
            }

        }

        cursor.close()

        events
                .filterNot { it.eTag == eTags[it.id] }
                .forEach {

                    val vEvent = Biweekly.parse(it.data).first().events.first()

                    val localId = localIds[it.id]

                    val eventCv = vEvent.toContentValues(it, localCalendarId)

                    if (localId != null) {

                        contentClient.update(
                                eventsUri,
                                eventCv,
                                "${CalendarContract.Events._ID} = ?",
                                arrayOf(localId.toString())
                        )

                    } else {

                        contentClient.insert(eventsUri, eventCv)

                    }

                }

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
        cv.put(CalendarContract.Calendars.DIRTY, 0)
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

    @Throws(IOException::class, ParseException::class)
    private fun VEvent.toContentValues(
            remote: RemoteCalendarEvent,
            localCalendarId: Long,
            localId: Long? = null
            /*, group: List<VEvent> = emptyList()*/
    ): ContentValues {

        return ContentValues().apply {

            localId?.let { put(CalendarContract.Events._ID, it) }

            put(Contract.Events.REMOTE_ID, remote.id)
            put(CalendarContract.Events.UID_2445, uid.value)
            val recurrenceId = recurrenceId?.value?.time ?: 0L
            put(Contract.Events.REQURENCE_ID, recurrenceId)
            put(Contract.Events.REMOTE_LAST_MODIFIED, remote.lastModified)
            put(Contract.Events.REMOTE_ETAG, remote.eTag)
            put(CalendarContract.Events._SYNC_ID, "${uid.value}-$recurrenceId")
            put(CalendarContract.Events.CALENDAR_ID, localCalendarId)

            put(CalendarContract.Events.DIRTY, 0)

            put(CalendarContract.Events.TITLE, summary?.value)
            put(CalendarContract.Events.DESCRIPTION, description?.value)
            put(CalendarContract.Events.EVENT_LOCATION, location?.value)

            val utcTimeZone = TimeZone.getTimeZone("UTC")
            val start = dateStart.value.rawComponents.toDate(utcTimeZone)
            val end = dateEnd.value.rawComponents.toDate(utcTimeZone)

            put(CalendarContract.Events.DTSTART, start.time)
            put(CalendarContract.Events.EVENT_TIMEZONE, utcTimeZone.id)

            put(CalendarContract.Events.DTEND, end.time)
            put(CalendarContract.Events.EVENT_END_TIMEZONE, utcTimeZone.id)

            duration?.let {
                val stringRepresentation = it.value.toString()
                put(CalendarContract.Events.DURATION, stringRepresentation)
            }

            val isAllDay = (end.time - start.time).let {
                it % TimeUnit.DAYS.toMillis(1) == 0L && it != 0L
            }

            put(CalendarContract.Events.ALL_DAY, if (isAllDay) 1 else 0)

            recurrenceRule?.value?.also {

                it.until?.also { put(CalendarContract.Events.LAST_DATE, it.time) }

                put(CalendarContract.Events.RRULE, it.toRRule())

                exceptionRules?.firstOrNull()?.also {
                    put(CalendarContract.Events.EXRULE, it.value.toRRule())
                }

                exceptionDates?.map {
                    it.values.map { it.rawComponents.toDate(utcTimeZone).time }
                } ?.let { dates ->
                    mutableListOf<Long>().apply {
                        dates.forEach { this += it }
                    }.toList()
                } ?.also {
                    put(CalendarContract.Events.EXDATE, it.joinToString())
                }

            }

        }

        /*
        val exdateBuilder = StringBuilder()
        val exdates = event.getProperties(Property.EXDATE)
        if (exdates != null) {
            for (i in 0 until exdates!!.size()) {
                val exdate = exdates!!.get(i) as ExDate
                exdate.setUtc(true)
                if (exdateBuilder.length > 0) {
                    exdateBuilder.append(',')
                }
                exdateBuilder.append(exdate.getDates().toString())
            }
        }
        if (recurrenceId == 0L && group.size > 0) {
            for (inner in group) {
                if (inner.getRecurrenceId() != null) {
                    val utcRecurrence = RecurrenceId(inner.getRecurrenceId().getValue())
                    utcRecurrence.setUtc(true)
                    if (exdateBuilder.length > 0) {
                        exdateBuilder.append(',')
                    }
                    exdateBuilder.append(utcRecurrence.getValue())
                }
            }
        }
        */
    }

    private fun Recurrence.toRRule(): String {

        return listOfNotNull(
                frequency?.name?.let { "FREQ=$it" },
                count?.let { "COUNT=$it" },
                interval?.let { "INTERVAL=$it" },
                bySecond?.firstOrNull()?.let { "BYSECOND=$it" },
                byMinute?.firstOrNull()?.let { "BYMINUTE=$it" },
                byHour?.firstOrNull()?.let { "BYHOUR=$it" },
                byDay?.firstOrNull()?.let { "BYDAY=$it" },
                byMonthDay?.firstOrNull()?.let { "BYMONTHDAY=$it" },
                byYearDay?.firstOrNull()?.let { "BYYEARDAY=$it" },
                byWeekNo?.firstOrNull()?.let { "BYWEEKNO=$it" },
                byMonth?.firstOrNull()?.let { "BYMONTH=$it" },
                bySetPos?.firstOrNull()?.let { "BYSETPOS=$it" },
                workweekStarts?.name?.let { "WKST=$it" }
                // TODO: xRules?
        ).joinToString(separator = ";")

    }

    class Factory @Inject constructor(
            private val dao: CalendarsDao,
            private val calendarMapper: CalendarMapper,
            private val remoteServiceProvider: CalendarRemoteServiceProvider
    ) {

        fun create(account: Account, client: ContentProviderClient) : CalendarSyncOperation {
            return CalendarSyncOperation(account, client, dao, calendarMapper, remoteServiceProvider)
        }

    }

}