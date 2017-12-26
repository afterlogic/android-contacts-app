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
import com.afterlogic.auroracontacts.data.util.RemoteServiceProvider
import com.afterlogic.auroracontacts.presentation.background.sync.CustomContact
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import timber.log.Timber
import java.io.IOException
import java.text.ParseException
import java.util.*
import javax.inject.Inject

class CalendarSyncOperation private constructor(
        private val account: Account,
        private val contentClient: ContentProviderClient,
        private val dao: CalendarsDao,
        private val calendarMapper: CalendarMapper,
        remoteServiceProvider: RemoteServiceProvider<CalendarRemoteService>
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

        val idsQuery = events.map { it.id } .toSqlIn()

        val eventsClient = contentClient.events
        // Delete unexists. Don't delete items without remote ID,
        // cause it can be first inverse local sync.
        eventsClient.delete(
                """
                    ${CalendarContract.Events.CALENDAR_ID} = $localCalendarId AND
                    ${CustomContact.Events.SYNCED} = 1 AND
                    ${CustomContact.Events.REMOTE_ID} NOT IN $idsQuery

                """.trimIndent()
        )

        val cursor = eventsClient.query(
                arrayOf(
                        CalendarContract.Events._ID,
                        CustomContact.Events.REMOTE_ID,
                        CustomContact.Events.REMOTE_ETAG
                ),
                "${CalendarContract.Events.CALENDAR_ID} = ?",
                arrayOf(localCalendarId.toString())
        ) ?: throw UnexpectedNullCursorException()

        data class LocalInfo(val localId: Long, val eTag: String?)

        val localData = cursor.toList {
            val remoteId = cursor.getString(CustomContact.Events.REMOTE_ID) ?: return@toList null
            val localId = cursor.getLong(0)
            val eTag = cursor.getString(CustomContact.Events.REMOTE_ETAG)
            remoteId to (LocalInfo(localId, eTag))
        }
                .filterNotNull()
                .associate { (id, local) -> id to local }

        cursor.close()

        events
                .filterNot { it.eTag == localData[it.id]?.eTag }
                .forEach {

                    val vEvent = Biweekly.parse(it.data).first().events.first()

                    val eventCv = vEvent.toContentValues(it, localCalendarId)

                    val localId = localData[it.id]?.localId
                    if (localId != null) {

                        eventsClient.update(
                                eventCv,
                                "${CalendarContract.Events._ID} = ?",
                                arrayOf(localId.toString())
                        )

                    } else {

                        eventsClient.insert(eventCv)

                    }

                }

    }

    private fun syncLocalEventsToRemote(localCalendarId: Long, remoteCalendar: RemoteCalendar): Single<Boolean> {

        return Single.concat(
                deleteLocallyDeleted(localCalendarId, remoteCalendar),
                uploadLocalChanges(localCalendarId, remoteCalendar)
        ).all { it }

    }

    private fun deleteLocallyDeleted(localCalendarId: Long, remoteCalendar: RemoteCalendar): Single<Boolean> {

        return Single.defer {

            val eventsClient = contentClient.events

            // Delete all locals which was not synced to remote
            eventsClient.delete(
                    selection = """
                    ${CalendarContract.Events.CALENDAR_ID} = $localCalendarId AND
                            ${CalendarContract.Events.DELETED} = 1 AND (
                                    ${CustomContact.Events.SYNCED} != 1 OR
                                    ${CustomContact.Events.REMOTE_ID} IS NULL
                            )
                    """.trimIndent()
            )

            val cursor = eventsClient.query(
                    projection = arrayOf(CustomContact.Events.REMOTE_ID),
                    selection = """
                    ${CalendarContract.Events.CALENDAR_ID} = $localCalendarId AND
                            ${CalendarContract.Events.DELETED} = 1
                    """.trimIndent()
            ) ?: throw UnexpectedNullCursorException()

            if (cursor.count == 0) {
                cursor.close()
                return@defer Single.just(true)
            }

            val deletedIds = cursor.toList { it.getString(CustomContact.Events.REMOTE_ID) }
                    .filterNotNull()

            cursor.close()

            val deleteRequest = DeleteCalendarEventsRequest(remoteCalendar.id, deletedIds)

            remoteService.flatMapCompletable { it.deleteEvent(deleteRequest) }
                    .doOnComplete {

                        val deleted = eventsClient.delete(
                                selection = "${CustomContact.Events.REMOTE_ID} IN ${deletedIds.toSqlIn()}"
                        )

                        Timber.d("Deleted: $deleted")

                    }
                    .andThen(Single.just(false))

        }

    }

    @SuppressLint("Recycle")
    private fun uploadLocalChanges(localCalendarId: Long, remoteCalendar: RemoteCalendar): Single<Boolean> {

        return Single.defer {

            val eventsClient = contentClient.events

            val cursor = eventsClient.query(
                    selection = """
                    ${CalendarContract.Events.CALENDAR_ID} = $localCalendarId AND
                            ${CalendarContract.Events.DIRTY} = 1
                    """.trimIndent()
            ) ?: throw UnexpectedNullCursorException()

            if (cursor.count == 0) {
                cursor.close()
                return@defer Single.just(true)
            }

            val newUuids = mutableMapOf<Long, String>()

            val updateEvents = cursor.toList {

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

                localId to UpdateCalendarEventRequest(
                        remoteId ?: "${vEvent.uid.value}.ics", remoteCalendar.id, data
                )

            } .associate { it }

            cursor.close()

            updateEvents.map { (localId, request) ->

                remoteService.flatMapCompletable { it.updateEvent(request) }
                        .doOnComplete {

                            val cv = ContentValues().apply {

                                put(CalendarContract.Events.DIRTY, 0)
                                put(CustomContact.Events.SYNCED, 1)
                                put(CustomContact.Events.REMOTE_ID, request.id)

                                newUuids[localId]?.also {
                                    put(CalendarContract.Events.UID_2445, it)
                                }

                            }

                            eventsClient.update(
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
            put(CalendarContract.Events._SYNC_ID, remote.id)
            put(CalendarContract.Events.UID_2445, uid.value)
            val recurrenceId = recurrenceId?.value?.time ?: 0L
            put(CustomContact.Events.REQURENCE_ID, recurrenceId)
            put(CustomContact.Events.SYNCED, 1)
            put(CustomContact.Events.REMOTE_ETAG, remote.eTag)
            put(CalendarContract.Events._SYNC_ID, "${uid.value}-$recurrenceId")

            put(CalendarContract.Events.DIRTY, 0)

            put(CalendarContract.Events.TITLE, summary?.value)
            put(CalendarContract.Events.DESCRIPTION, description?.value)
            put(CalendarContract.Events.EVENT_LOCATION, location?.value)

            val utcTimeZone = TimeZone.getTimeZone("UTC")
            val start = dateStart.value.rawComponents.toDate(utcTimeZone)

            put(CalendarContract.Events.DTSTART, start.time)
            put(CalendarContract.Events.EVENT_TIMEZONE, utcTimeZone.id)

            dateEnd?.value?.rawComponents?.toDate(utcTimeZone)?.also {

                put(CalendarContract.Events.DTEND, it.time)
                put(CalendarContract.Events.EVENT_END_TIMEZONE, utcTimeZone.id)

            }

            duration?.let {
                val stringRepresentation = it.value.toString()
                put(CalendarContract.Events.DURATION, stringRepresentation)
            }

            val isAllDay = !dateStart.value.hasTime()

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
                bySecond?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYSECOND=$it" },
                byMinute?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYMINUTE=$it" },
                byHour?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYHOUR=$it" },
                byDay?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYDAY=$it" },
                byMonthDay?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYMONTHDAY=$it" },
                byYearDay?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYYEARDAY=$it" },
                byWeekNo?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYWEEKNO=$it" },
                byMonth?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYMONTH=$it" },
                bySetPos?.joinToString(",")?.takeIf { it.isNotBlank() }?.let { "BYSETPOS=$it" },
                workweekStarts?.name?.takeIf { it.isNotBlank() }?.let { "WKST=$it" }
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
            ruleMap["BYSECOND"]?.toIntList()?.takeIf { it.isNotEmpty() }?.also { bySecond(it) }
            ruleMap["BYMINUTE"]?.toIntList()?.takeIf { it.isNotEmpty() }?.also { byMinute(it) }
            ruleMap["BYHOUR"]?.toIntList()?.takeIf { it.isNotEmpty() }?.also { byHour(it) }
            ruleMap["BYDAY"]?.toList { DayOfWeek.valueOfAbbr(it) } ?.takeIf { it.isNotEmpty() }?.also { byDay(it) }
            ruleMap["BYMONTHDAY"]?.toIntList()?.takeIf { it.isNotEmpty() }?.also { byMonthDay() }
            ruleMap["BYYEARDAY"]?.toIntList()?.takeIf { it.isNotEmpty() }?.also { byYearDay(it) }
            ruleMap["BYWEEKNO"]?.toIntList()?.takeIf { it.isNotEmpty() }?.also { byWeekNo(it) }
            ruleMap["BYMONTH"]?.toIntList()?.takeIf { it.isNotEmpty() }?.also { byMonth(it) }
            ruleMap["BYSETPOS"]?.toIntList()?.takeIf { it.isNotEmpty() }?.also { bySetPos(it) }
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

            val isAllDay = getLong(CalendarContract.Events.ALL_DAY) == 1L

            val date = { timeColumn: String, tzColumn: String, hasTime: Boolean ->

                getLong(timeColumn)?.let {
                    val tz = getString(tzColumn)?.let { TimeZone.getTimeZone(it) } ?:
                            TimeZone.getTimeZone("UTC")
                    val dateComponents = DateTimeComponents(Date(it), tz)
                    ICalDate(dateComponents, hasTime)
                }

            }

            date(CalendarContract.Events.DTSTART, CalendarContract.Events.EVENT_TIMEZONE, !isAllDay)?.also {
                dateStart = DateStart(it)
            }

            date(CalendarContract.Events.DTEND, CalendarContract.Events.EVENT_END_TIMEZONE, !isAllDay)?.also {
                dateEnd = DateEnd(it)
            }

            getString(CalendarContract.Events.DURATION)?.also {
                duration = DurationProperty(Duration.parse(it))
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

    inline private fun <T> Cursor.toList(mapper: (Cursor) -> T): List<T> {
        val list = mutableListOf<T>()
        while (moveToNext()) {
            list.add(mapper(this))
        }
        return list.toList()
    }

    private fun <T> String.toList(map: (String) -> T): List<T> {
        return this.split(",").map { map(it.trim()) }
    }

    private fun String.toIntList(): List<Int> = this.toList { it.toInt() }

    private fun List<String>.toSqlIn(): String = joinToString("', '", prefix = "('", postfix = "')")

    class Factory @Inject constructor(
            private val dao: CalendarsDao,
            private val calendarMapper: CalendarMapper,
            private val remoteServiceProvider: RemoteServiceProvider<CalendarRemoteService>
    ) {

        fun create(account: Account, client: ContentProviderClient) : CalendarSyncOperation {
            return CalendarSyncOperation(account, client, dao, calendarMapper, remoteServiceProvider)
        }

    }

    private val ContentProviderClient.calendars: ContentClientHelper get() {
        return ContentClientHelper(this, getSyncUri(CalendarContract.Calendars.CONTENT_URI))
    }

    private val ContentProviderClient.events: ContentClientHelper get() {
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

        fun delete(selection: String, selectionArgs: Array<String>? = null): Int {
            return client.delete(uri, selection, selectionArgs)
        }

    }

}