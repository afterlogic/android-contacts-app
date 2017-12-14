package com.afterlogic.auroracontacts.presentation.background.sync.calendar

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import biweekly.Biweekly
import biweekly.ICalVersion
import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.property.*
import biweekly.util.*
import com.afterlogic.auroracontacts.data.calendar.*
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.presentation.background.sync.CustomContact
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import io.reactivex.Completable
import io.reactivex.Single
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

    private val remoteService  = remoteServiceProvider.get()

    fun sync() : Completable = Completable.defer {

        val remoteCalendars = remoteService.flatMap { it.getCalendars() }

        val local = dao.all.map { it.map { calendarMapper.toPlain(it) } }.firstOrError()

        var round = 0
        var success = false

        Singles.zip(remoteCalendars, local) { rem, loc ->
            val syncable = loc.filter { it.settings.syncEnabled }.map { it.id }
            rem.filter { syncable.contains(it.id) }
        }
                .flatMap(this::syncRound)
                .doOnSuccess { success = it }
                .doOnSubscribe { round++ }
                .repeatUntil { round == 5 || success }
                .ignoreElements()

    }

    @SuppressLint("Recycle")
    fun syncRound(calendars: List<RemoteCalendar>): Single<Boolean> = Single.defer {

        // Delete unsyncable calendars
        val queryIds = calendars.joinToString("', '", "('", "')") { it.id }

        val calendarClient = contentClient.calendars

        calendarClient.delete("${CustomContact.Calendar.REMOTE_ID} NOT IN $queryIds")

        val cursor = calendarClient.query(
                arrayOf(
                        CalendarContract.Calendars._ID,
                        CustomContact.Calendar.REMOTE_ID,
                        CustomContact.Calendar.REMOTE_CTAG
                )
        ) ?: throw UnexpectedNullCursorException()

        val localCTags = mutableMapOf<String, String>()
        val localIds = mutableMapOf<String, Long>()

        while (cursor.moveToNext()) {

            val id = cursor.getString(1)

            localIds[id] = cursor.getLong(0)

            if (!cursor.isNull(2)) {
                localCTags[id] = cursor.getString(2)
            }

        }

        cursor.close()

        fun RemoteCalendar.isChanged() : Boolean = cTag != localCTags[id]

        // Update calendars which changed
        calendars.forEach {

            if (!it.isChanged()) return@forEach

            val localId = localIds[it.id]

            val cv = it.toContentValues()

            if (localId != null) {

                calendarClient.update(cv, "${CalendarContract.Calendars._ID} = $localId")

            } else {

                val id = calendarClient.insert(cv).lastPathSegment.toLong()
                localIds[it.id] = id

            }

        }

        // Sync events (remote only if calendar cTag changed) and local
        calendars.map { cal ->

            val syncRemoteEventsToLocal = if (cal.isChanged()) {

                syncRemoteEventsToLocal(cal, localIds[cal.id]!!)
                        .doOnComplete {

                            val cTag = ContentValues()
                            cTag.put(CustomContact.Calendar.REMOTE_CTAG, cal.cTag)

                            calendarClient.update(
                                    cTag,
                                    "${CustomContact.Calendar.REMOTE_ID} = ?",
                                    arrayOf(cal.id)
                            )

                        }

            } else {

                Completable.complete()

            }

            val localId = localIds[cal.id]!!

            syncRemoteEventsToLocal.andThen(syncLocalEventsToRemote(localId, cal))

        } .let {
            Single.concat(it).all { it }
        }

    }

    private fun syncRemoteEventsToLocal(calendar: RemoteCalendar, localId: Long): Completable {

        return remoteService.flatMap { it.getEvents(calendar.id) }
                .flatMapCompletable { Completable.fromAction { storeEvents(localId, it)  } }

    }

    @SuppressLint("Recycle")
    private fun storeEvents(localCalendarId: Long, events: List<RemoteCalendarEvent>) {

        val eventsUri = getSyncUri(CalendarContract.Events.CONTENT_URI)

        val idsQuery = events.joinToString("', '", "('", "')") { it.id }

        // Delete unexists. Don't delete items without remote ID,
        // cause it can be first inverse local sync.
        contentClient.delete(
                eventsUri,
                """
                    ${CalendarContract.Events.CALENDAR_ID} = $localCalendarId AND
                            ${CustomContact.Calendar.REMOTE_ID} IS NOT NULL AND
                            ${CustomContact.Calendar.REMOTE_ID} NOT IN $idsQuery

                """.trimIndent(),
                null

        )

        val cursor = contentClient.query(
                eventsUri,
                arrayOf(
                        CalendarContract.Events._ID,
                        CustomContact.Events.REMOTE_ID,
                        CustomContact.Events.REMOTE_ETAG,
                        CalendarContract.Events.UID_2445
                ),
                "${CalendarContract.Events.CALENDAR_ID} = ?",
                arrayOf(localCalendarId.toString()),
                null
        ) ?: throw UnexpectedNullCursorException()

        val eTags = mutableMapOf<String, String>()
        val localIds = mutableMapOf<String, Long>()
        val newUids = mutableMapOf<String, Long>()

        while (cursor.moveToNext()) {

            val remoteId = cursor.getString(CustomContact.Events.REMOTE_ID)
            val localId = cursor.getLong(0)

            if (remoteId != null) {

                localIds[remoteId] = localId

                cursor.getString(CustomContact.Events.REMOTE_ETAG)?.also {
                    eTags[remoteId] = it
                }

            } else {

                cursor.getString(CalendarContract.Events.UID_2445)?.also {
                    newUids[it] = localId
                }

            }

        }

        cursor.close()

        events
                .filterNot { it.eTag == eTags[it.id] }
                .forEach {

                    val vEvent = Biweekly.parse(it.data).first().events.first()

                    val localId = localIds[it.id] ?: vEvent.uid?.let { newUids[it.value] }

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

        // Delete all not dirty items without remote ID, cause they not exists.
        contentClient.delete(
                eventsUri,
                """
                    ${CalendarContract.Events.CALENDAR_ID} = $localCalendarId AND
                            ${CustomContact.Calendar.REMOTE_ID} IS NULL AND
                            ${CalendarContract.Events.DIRTY} = 0

                """.trimIndent(),
                null

        )

    }

    @SuppressLint("Recycle")
    private fun syncLocalEventsToRemote(localCalendarId: Long, remoteCalendar: RemoteCalendar): Single<Boolean> {

        return Single.defer {

            val eventsUri = getSyncUri(CalendarContract.Events.CONTENT_URI)

            val cursor = contentClient.query(
                    eventsUri,
                    null,
                    """
                    ${CalendarContract.Events.CALENDAR_ID} = $localCalendarId AND
                            ${CalendarContract.Events.DIRTY} = 1
                """.trimIndent(),
                    null, null
            ) ?: throw UnexpectedNullCursorException()

            if (cursor.count == 0) {
                cursor.close()
                return@defer Single.just(true)
            }

            val updateEvents = mutableMapOf<Long, UpdateCalendarEventRequest>()
            val newUuids = mutableMapOf<Long, String>()

            while (cursor.moveToNext()) {

                val localId = cursor.getLong(CalendarContract.Events._ID)!!
                val remoteId = cursor.getString(CustomContact.Events.REMOTE_ID)

                val vEvent = cursor.toVEvent()

                if (vEvent.uid == null) {
                    val uid = UUID.randomUUID().toString().toUpperCase()
                    newUuids[localId] = uid
                    vEvent.uid = Uid(uid)
                }

                val iCal = ICalendar()
                iCal.events.add(vEvent)
                iCal.calendarScale = CalendarScale.gregorian()
                iCal.version = ICalVersion.V2_0
                val data = iCal.write()

                updateEvents[localId] = UpdateCalendarEventRequest(
                    remoteId ?: "${vEvent.uid.value}.ics", remoteCalendar.id, data
                )

            }

            cursor.close()

            updateEvents.map { (localId, request) ->

                remoteService.flatMapCompletable { it.updateEvent(request) }
                        .doOnComplete {

                            val cv = ContentValues().apply {

                                put(CalendarContract.Events.DIRTY, 0)

                                newUuids[localId]?.also {
                                    put(CalendarContract.Events.UID_2445, it)
                                }

                            }

                            contentClient.update(
                                    eventsUri,
                                    cv,
                                    "${CalendarContract.Events._ID} = ?",
                                    arrayOf(localId.toString())
                            )

                        }


            } .let {

                Completable.concat(it).andThen(Single.just(false))

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
            timeZone: TimeZone = TimeZone.getDefault(),
            addCTag: Boolean = false
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
        cv.put(CustomContact.Calendar.REMOTE_ID, this.id)
        cv.put(CalendarContract.Calendars.DIRTY, 0)

        if (addCTag) {
            cv.put(CustomContact.Calendar.REMOTE_CTAG, this.cTag)
        }

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
    ): ContentValues {

        return ContentValues().apply {

            put(CalendarContract.Events.CALENDAR_ID, localCalendarId)
            localId?.let { put(CalendarContract.Events._ID, it) }

            put(CustomContact.Events.REMOTE_ID, remote.id)
            put(CalendarContract.Events.UID_2445, uid.value)
            val recurrenceId = recurrenceId?.value?.time ?: 0L
            put(CustomContact.Events.REQURENCE_ID, recurrenceId)
            put(CustomContact.Events.REMOTE_LAST_MODIFIED, remote.lastModified)
            put(CustomContact.Events.REMOTE_ETAG, remote.eTag)
            put(CalendarContract.Events._SYNC_ID, "${uid.value}-$recurrenceId")

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

            }

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

    private fun Recurrence.toRRule(): String {

        return listOfNotNull(
                frequency?.name?.let { "FREQ=$it" },
                count?.let { "COUNT=$it" },
                interval?.let { "INTERVAL=$it" },
                bySecond?.joinToString(",")?.let { "BYSECOND=$it" },
                byMinute?.joinToString(",")?.let { "BYMINUTE=$it" },
                byHour?.joinToString(",")?.let { "BYHOUR=$it" },
                byDay?.joinToString(",")?.let { "BYDAY=$it" },
                byMonthDay?.joinToString(",")?.let { "BYMONTHDAY=$it" },
                byYearDay?.joinToString(",")?.let { "BYYEARDAY=$it" },
                byWeekNo?.joinToString(",")?.let { "BYWEEKNO=$it" },
                byMonth?.joinToString(",")?.let { "BYMONTH=$it" },
                bySetPos?.joinToString(",")?.let { "BYSETPOS=$it" },
                workweekStarts?.name?.let { "WKST=$it" }
                // TODO: xRules?
        ).joinToString(separator = ";")

    }

    private fun parseRecurrence(rule: String, until: Long? = null): Recurrence {

        val ruleMap = rule.split(";")
                .filterNot { it.isBlank() }
                .map { it.split("=") }
                .associate { it[0] to it[1] }

        return Recurrence.Builder(Frequency.valueOf(ruleMap["FREQ"]!!)).apply {

            ruleMap["COUNT"]?.also { count(it.toInt()) }
            ruleMap["INTERVAL"]?.also { interval(it.toInt()) }
            ruleMap["BYSECOND"]?.toIntList()?.also { bySecond(it) }
            ruleMap["BYMINUTE"]?.toIntList()?.also { byMinute(it) }
            ruleMap["BYHOUR"]?.toIntList()?.also { byHour(it) }
            ruleMap["BYDAY"]?.toList { DayOfWeek.valueOfAbbr(it) } ?.also { byDay(it) }
            ruleMap["BYMONTHDAY"]?.toIntList()?.also { byMonthDay() }
            ruleMap["BYYEARDAY"]?.toIntList()?.also { byYearDay(it) }
            ruleMap["BYWEEKNO"]?.toIntList()?.also { byWeekNo(it) }
            ruleMap["BYMONTH"]?.toIntList()?.also { byMonth(it) }
            ruleMap["BYSETPOS"]?.toIntList()?.also { bySetPos(it) }
            ruleMap["WKST"]?.also { workweekStarts(DayOfWeek.valueOfAbbr(it)) }

            until?.also { until(Date(it)) }


        }.build()

    }

    private fun Cursor.toVEvent(): VEvent {

        return VEvent().apply {

            getString(CalendarContract.Events.UID_2445)?.also { uid = Uid(it) }
            getLong(CustomContact.Events.REQURENCE_ID)?.also { recurrenceId = RecurrenceId(Date(it)) }

            lastModified = LastModified(Date())

            getString(CalendarContract.Events.TITLE)?.also { summary = Summary(it) }
            getString(CalendarContract.Events.DESCRIPTION)?.also { description = Description(it) }
            getString(CalendarContract.Events.EVENT_LOCATION)?.also { location = Location(it) }

            // TODO: Timezone?
            //val utcTimeZone = TimeZone.getTimeZone("UTC")
            //val start = dateStart.value.rawComponents.toDate(utcTimeZone)
            //val end = dateEnd.value.rawComponents.toDate(utcTimeZone)

            getLong(CalendarContract.Events.DTSTART)?.also { dateStart = DateStart(Date(it)) }

            if (getLong(CalendarContract.Events.ALL_DAY) == 1L) {

            } else {
                getLong(CalendarContract.Events.DTEND)?.also { dateEnd = DateEnd(Date(it)) }
                getString(CalendarContract.Events.DURATION)?.also { duration = DurationProperty(Duration.parse(it)) }
            }

            getString(CalendarContract.Events.RRULE)
                    ?.let { parseRecurrence(it, getLong(CalendarContract.Events.LAST_DATE)) }
                    ?.let { RecurrenceRule(it) }
                    ?.also { recurrenceRule = it }

            getString(CalendarContract.Events.EXRULE)
                    ?.let { parseRecurrence(it) }
                    ?.let { ExceptionRule(it) }
                    ?.also { exceptionRules.add(it) }

            getString(CalendarContract.Events.EXDATE)
                    ?.split(",")
                    ?.filterNot { it.isBlank() }
                    ?.map { ICalDate(Date(it.toLong())) }
                    ?.also {
                        val dates = ExceptionDates()
                        dates.values.addAll(it)
                        exceptionDates.add(dates)
                    }

        }

    }

    private fun Cursor.getString(columnName: String, canBeBlank: Boolean = false) : String? {
        return getColumnIndex(columnName)
                .takeIf { it != -1 && !isNull(it) }
                ?.let { getString(it) }
                ?.takeIf { canBeBlank || it.isNotBlank() }
    }

    private fun Cursor.getLong(columnName: String, canMinusOne: Boolean = false) : Long? {
        return getColumnIndex(columnName)
                .takeIf { it != -1 && !isNull(it) }
                ?.let { getLong(it) }
                ?.takeIf { canMinusOne || it != -1L }
    }

    private fun <T> String.toList(map: (String) -> T): List<T> {
        return this.split(",").map { map(it.trim()) }
    }

    private fun String.toIntList(): List<Int> = this.toList { it.toInt() }

    class Factory @Inject constructor(
            private val dao: CalendarsDao,
            private val calendarMapper: CalendarMapper,
            private val remoteServiceProvider: CalendarRemoteServiceProvider
    ) {

        fun create(account: Account, client: ContentProviderClient) : CalendarSyncOperation {
            return CalendarSyncOperation(account, client, dao, calendarMapper, remoteServiceProvider)
        }

    }

    private val ContentProviderClient.calendars: ContentClientHelper get() {
        return ContentClientHelper(this, getSyncUri(CalendarContract.Calendars.CONTENT_URI))
    }

    private val ContentProviderClient.eventss: ContentClientHelper get() {
        return ContentClientHelper(this, getSyncUri(CalendarContract.Events.CONTENT_URI))
    }

    private class ContentClientHelper(private val client: ContentProviderClient, private val uri: Uri) {

        fun update(values: ContentValues, selection: String, selectionArgs: Array<String>? = null) {
            client.update(uri, values, selection, selectionArgs)
        }

        fun insert(values: ContentValues): Uri {
            return client.insert(uri, values)
        }

        fun query(projection: Array<String>? = null, selection: String = "1", selectionArgs: Array<String>? = null, sortOrder: String? = null): Cursor? {
            return client.query(uri, projection, selection, selectionArgs, sortOrder)
        }

        fun delete(selection: String, selectionArgs: Array<String>? = null) {
            client.delete(uri, selection, selectionArgs)
        }

    }

}