package com.afterlogic.auroracontacts.presentation.background.sync.calendar

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.ContentProviderClient
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.provider.CalendarContract
import biweekly.Biweekly
import biweekly.ICalVersion
import biweekly.ICalendar
import biweekly.component.VAlarm
import biweekly.component.VEvent
import biweekly.property.*
import biweekly.util.*
import com.afterlogic.auroracontacts.core.util.ContentClientHelper
import com.afterlogic.auroracontacts.data.calendar.*
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.data.util.RemoteServiceProvider
import com.afterlogic.auroracontacts.presentation.background.sync.BaseSyncOperation
import com.afterlogic.auroracontacts.presentation.background.sync.CustomContract
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import com.afterlogic.auroracontacts.presentation.background.sync.contacts.ContactsSyncOperation
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import timber.log.Timber
import java.io.IOException
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private typealias Events = CalendarContract.Events
private typealias AEvents = CustomContract.Events
private typealias Reminders = CalendarContract.Reminders
private typealias Attendees = CalendarContract.Attendees
private typealias CAttendees = CustomContract.Events.Attendees

class CalendarsSyncOperation private constructor(
        private val account: Account,
        private val contentClient: ContentProviderClient,
        private val dao: CalendarsDao,
        private val calendarMapper: CalendarMapper,
        remoteServiceProvider: RemoteServiceProvider<CalendarRemoteService>
) : BaseSyncOperation(account, contentClient) {

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

        calendarClient.delete("${CustomContract.Calendar.REMOTE_ID} NOT IN $queryIds")

        val cursor = calendarClient.query(
                arrayOf(
                        CalendarContract.Calendars._ID,
                        CustomContract.Calendar.REMOTE_ID,
                        CustomContract.Calendar.REMOTE_CTAG
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
                            cTag.put(CustomContract.Calendar.REMOTE_CTAG, cal.cTag)

                            calendarClient.update(
                                    cTag,
                                    "${CustomContract.Calendar.REMOTE_ID} = ?",
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
                    ${Events.CALENDAR_ID} = $localCalendarId AND
                    ${AEvents.SYNCED} = 1 AND
                    ${AEvents.REMOTE_ID} NOT IN $idsQuery

                """.trimIndent()
        )

        val cursor = eventsClient.query(
                arrayOf(
                        Events._ID,
                        AEvents.REMOTE_ID,
                        AEvents.REMOTE_ETAG,
                        AEvents.ATTENDEES_ETAG,
                        AEvents.ALARMS_ETAG
                ),
                "${Events.CALENDAR_ID} = ?",
                arrayOf(localCalendarId.toString())
        ) ?: throw UnexpectedNullCursorException()

        data class LocalInfo(
                val Id: Long, val eTag: String?,
                val attendeeETag: String?, val alarmsETag: String?
        )

        val localData = cursor
                .toList {

                    val remoteId = cursor.getString(AEvents.REMOTE_ID) ?: return@toList null
                    val localId = cursor.getLong(0)
                    val eTag = cursor.getString(AEvents.REMOTE_ETAG)
                    val attendeesETag = cursor.getString(AEvents.ATTENDEES_ETAG)
                    val alarmsETag = cursor.getString(AEvents.ALARMS_ETAG)

                    remoteId to (LocalInfo(localId, eTag, attendeesETag, alarmsETag))

                }
                .filterNotNull()
                .associate { (id, local) -> id to local }

        cursor.close()

        events
                .filterNot { it.eTag == localData[it.id]?.eTag }
                .forEach {

                    val vEvent = Biweekly.parse(it.data).first().events.first()

                    val eventCv = vEvent.toContentValues(it, localCalendarId)

                    Attendees.EVENT_ID

                    val localInfo = localData[it.id]

                    if (localInfo != null) {

                        eventsClient.update(eventCv, "${Events._ID} = ${localInfo.Id}")

                        if (localInfo.attendeeETag != eventCv.getAsString(AEvents.ATTENDEES_ETAG)) {
                            storeAttendees(localInfo.Id, vEvent.attendees)
                        }

                        if (localInfo.alarmsETag != eventCv.getAsString(AEvents.ALARMS_ETAG)) {
                            storeAlarms(localInfo.Id, vEvent.alarms)
                        }

                    } else {

                        val newId = eventsClient.insert(eventCv).let { ContentUris.parseId(it) }
                        storeAttendees(newId, vEvent.attendees)
                        storeAlarms(newId, vEvent.alarms)

                    }

                }

    }

    private fun storeAttendees(eventLocalId: Long, attendees: List<Attendee>) {

        val client = contentClient.attendees

        client.delete("${Attendees.EVENT_ID} = $eventLocalId")

        attendees.forEach {
            val cv = it.toContentValues(eventLocalId)
            client.insert(cv)
        }

    }

    private fun storeAlarms(eventLocalId: Long, alarms: List<VAlarm>) {

        val client = contentClient.reminders

        client.delete("${Reminders.EVENT_ID} = $eventLocalId")

        alarms.forEach {
            val cv = it.toContentValues(eventLocalId)
            client.insert(cv)
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
                    ${Events.CALENDAR_ID} = $localCalendarId AND
                            ${Events.DELETED} = 1 AND (
                                    ${AEvents.SYNCED} != 1 OR
                                    ${AEvents.REMOTE_ID} IS NULL
                            )
                    """.trimIndent()
            )

            val cursor = eventsClient.query(
                    projection = arrayOf(AEvents.REMOTE_ID),
                    selection = """
                    ${Events.CALENDAR_ID} = $localCalendarId AND
                            ${Events.DELETED} = 1
                    """.trimIndent()
            ) ?: throw UnexpectedNullCursorException()

            if (cursor.count == 0) {
                cursor.close()
                return@defer Single.just(true)
            }

            val deletedIds = cursor.toList { it.getString(AEvents.REMOTE_ID) }
                    .filterNotNull()

            cursor.close()

            val deleteRequest = DeleteCalendarEventsRequest(remoteCalendar.id, deletedIds)

            remoteService.flatMapCompletable { it.deleteEvent(deleteRequest) }
                    .doOnComplete {

                        val deleted = eventsClient.delete(
                                selection = "${AEvents.REMOTE_ID} IN ${deletedIds.toSqlIn()}"
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
                    ${Events.CALENDAR_ID} = $localCalendarId AND
                            ${Events.DIRTY} = 1
                    """.trimIndent()
            ) ?: throw UnexpectedNullCursorException()

            if (cursor.count == 0) {
                cursor.close()
                return@defer Single.just(true)
            }

            val newUuids = mutableMapOf<Long, String>()

            val updateEvents = cursor.toList {

                val localId = cursor.getLong(Events._ID)!!
                val remoteId = cursor.getString(AEvents.REMOTE_ID)

                val vEvent = cursor.toVEvent()

                val remindersCursor = contentClient.reminders.query(
                        null,
                        "${Reminders.EVENT_ID} = $localId"
                ) ?: throw UnexpectedNullCursorException()

                remindersCursor.toList { it.toAlarm() } .forEach { vEvent.addAlarm(it) }

                remindersCursor.close()

                val attendeesCursor = contentClient.attendees.query(
                        null,
                        "${Attendees.EVENT_ID} = $localId"
                ) ?: throw UnexpectedNullCursorException()

                attendeesCursor.toList { it.toAttendee() }
                        .filterNotNull()
                        .filterNot { it.email == account.name || it.commonName == account.name }
                        .forEach { vEvent.addAttendee(it) }

                attendeesCursor.close()

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

                                put(Events.DIRTY, 0)
                                put(AEvents.SYNCED, 1)
                                put(AEvents.REMOTE_ID, request.id)

                                newUuids[localId]?.also {
                                    put(Events.UID_2445, it)
                                }

                            }

                            eventsClient.update(
                                    cv,
                                    "${Events._ID} = ?",
                                    arrayOf(localId.toString())
                            )

                        }

            } .let {

                Completable.concat(it).andThen(Single.just(false))

            }

        }

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
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name)
        cv.put(CalendarContract.Calendars.CALENDAR_COLOR, color)
        cv.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.id)
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, owner)
        cv.put(CustomContract.Calendar.REMOTE_ID, id)
        cv.put(CalendarContract.Calendars.DIRTY, 0)

        cv.put(CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES, "${Attendees.TYPE_NONE}")
        cv.put(CalendarContract.Calendars.ALLOWED_REMINDERS, "${Reminders.METHOD_ALERT}")
        cv.put(CalendarContract.Calendars.ALLOWED_AVAILABILITY, 0)

        if (addCTag) {
            cv.put(CustomContract.Calendar.REMOTE_CTAG, cTag)
        }

        cv.put(
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                if (this.accessLevel == RemoteCalendar.AccessLevel.EDITOR)
                    CalendarContract.Calendars.CAL_ACCESS_EDITOR
                else CalendarContract.Calendars.CAL_ACCESS_READ
        )

        return cv

    }

    @Throws(IOException::class, ParseException::class)
    private fun VEvent.toContentValues(
            remote: RemoteCalendarEvent,
            localCalendarId: Long,
            localId: Long? = null
    ): ContentValues = ContentValues().apply {

        put(Events.CALENDAR_ID, localCalendarId)
        localId?.let { put(Events._ID, it) }

        put(AEvents.REMOTE_ID, remote.id)
        put(Events._SYNC_ID, remote.id)
        put(Events.UID_2445, uid.value)
        val recurrenceId = recurrenceId?.value?.time ?: 0L
        put(AEvents.REQURENCE_ID, recurrenceId)
        put(AEvents.SYNCED, 1)
        put(AEvents.REMOTE_ETAG, remote.eTag)
        put(Events._SYNC_ID, "${uid.value}-$recurrenceId")

        put(Events.DIRTY, 0)

        put(Events.TITLE, summary?.value)
        put(Events.DESCRIPTION, description?.value)
        put(Events.EVENT_LOCATION, location?.value)

        val utcTimeZone = TimeZone.getTimeZone("UTC")
        val start = dateStart.value.rawComponents.toDate(utcTimeZone)

        put(Events.DTSTART, start.time)
        put(Events.EVENT_TIMEZONE, utcTimeZone.id)

        dateEnd?.value?.rawComponents?.toDate(utcTimeZone)?.also {

            put(Events.DTEND, it.time)
            put(Events.EVENT_END_TIMEZONE, utcTimeZone.id)

        }

        duration?.let {
            val stringRepresentation = it.value.toString()
            put(Events.DURATION, stringRepresentation)
        }

        val isAllDay = !dateStart.value.hasTime()

        put(Events.ALL_DAY, if (isAllDay) 1 else 0)

        recurrenceRule?.value?.also {

            it.until?.also { put(Events.LAST_DATE, it.time) }

            put(Events.RRULE, it.toRRule())

        }

        exceptionRules?.firstOrNull()?.also {
            put(Events.EXRULE, it.value.toRRule())
        }

        exceptionDates?.map {
            it.values.map { it.rawComponents.toDate(utcTimeZone).time }
        } ?.let { dates ->
            mutableListOf<Long>().apply {
                dates.forEach { this += it }
            }.toList()
        } ?.also {
            put(Events.EXDATE, it.joinToString())
        }

        val attendeesETag = (attendees ?: emptyList())
                .joinToString(separator = ":") {
                    ContactsSyncOperation.DigestUtil.toSha256(it.toString())
                }
                .let { ContactsSyncOperation.DigestUtil.toSha256(it) }

        put(CustomContract.Events.ATTENDEES_ETAG, attendeesETag)
        put(Events.HAS_ATTENDEE_DATA, (attendees?.size ?: 0) > 0 )

        val alarmsETag = (alarms ?: emptyList())
                .joinToString(separator = ":") {
                    ContactsSyncOperation.DigestUtil.toSha256(it.toString())
                }
                .let { ContactsSyncOperation.DigestUtil.toSha256(it) }

        put(CustomContract.Events.ALARMS_ETAG, alarmsETag)
        put(Events.HAS_ALARM, (alarms?.size ?: 0) > 0)

    }

    private fun Attendee.toContentValues(eventId: Long): ContentValues = ContentValues().putall(

            Attendees.EVENT_ID to eventId,

            Attendees.ATTENDEE_NAME to (commonName ?: email),
            Attendees.ATTENDEE_EMAIL to email,
            Attendees.ATTENDEE_STATUS to Attendees.ATTENDEE_STATUS_NONE,
            CustomContract.Events.Attendees.PARITCIPATION_STATUS to participationStatus?.value,
            Attendees.ATTENDEE_RELATIONSHIP to Attendees.RELATIONSHIP_NONE,
            CustomContract.Events.Attendees.ROLE to role?.value,
            Attendees.ATTENDEE_TYPE to Attendees.TYPE_NONE,
            CustomContract.Events.Attendees.PARITCIPATION_LEVEL to participationLevel?.toString()

    )

    private fun VAlarm.toContentValues(eventId: Long) = ContentValues().putall(
            Reminders.EVENT_ID to eventId,
            Reminders.METHOD to Reminders.METHOD_ALERT,
            Reminders.MINUTES to (trigger?.durationInMunites ?: 0L)
    )
    
    private val Trigger.durationInMunites: Long get() {
        return (duration ?: Duration.fromMillis(0) )
                .let {
                    listOf(
                            it.weeks?.let { it * 7 } to TimeUnit.DAYS,
                            it.days to TimeUnit.DAYS,
                            it.hours to TimeUnit.HOURS,
                            it.minutes to TimeUnit.MINUTES
                    ).filter { it.first != null }
                            .map { it.second.toMinutes(it.first.toLong()) }
                            .sum()
                }
    }

    private fun Recurrence.toRRule(): String {
        
        fun List<Any>.mapToValue(name: String): String? = joinToString(",")
                .takeIf { it.isNotBlank() }
                ?.let { "$name=$it" }

        return listOfNotNull(
                frequency?.name?.let { "FREQ=$it" },
                count?.let { "COUNT=$it" },
                interval?.let { "INTERVAL=$it" },
                bySecond?.mapToValue("BYSECOND"),
                byMinute?.mapToValue("BYMINUTE"),
                byHour?.mapToValue("BYHOUR"),
                byDay?.mapToValue("BYDAY"),
                byMonthDay?.mapToValue("BYMONTHDAY"),
                byYearDay?.mapToValue("BYYEARDAY"),
                byWeekNo?.mapToValue("BYWEEKNO"),
                byMonth?.mapToValue("BYMONTH"),
                bySetPos?.mapToValue("BYSETPOS"),
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

            fun <T> List<T>.takeIfNotEmpty(): List<T>? = takeIf { it.isNotEmpty() }
            fun String.toNotEmptyIntList(): List<Int>? = toIntList().takeIfNotEmpty()

            ruleMap["COUNT"]?.also { count(it.toInt()) }
            ruleMap["INTERVAL"]?.also { interval(it.toInt()) }
            ruleMap["BYSECOND"]?.toNotEmptyIntList()?.also { bySecond(it) }
            ruleMap["BYMINUTE"]?.toNotEmptyIntList()?.also { byMinute(it) }
            ruleMap["BYHOUR"]?.toNotEmptyIntList()?.also { byHour(it) }
            ruleMap["BYDAY"]?.toList { DayOfWeek.valueOfAbbr(it) } ?.takeIfNotEmpty()?.also { byDay(it) }
            ruleMap["BYMONTHDAY"]?.toNotEmptyIntList()?.also { byMonthDay() }
            ruleMap["BYYEARDAY"]?.toNotEmptyIntList()?.also { byYearDay(it) }
            ruleMap["BYWEEKNO"]?.toNotEmptyIntList()?.also { byWeekNo(it) }
            ruleMap["BYMONTH"]?.toNotEmptyIntList()?.also { byMonth(it) }
            ruleMap["BYSETPOS"]?.toNotEmptyIntList()?.also { bySetPos(it) }
            ruleMap["WKST"]?.also { workweekStarts(DayOfWeek.valueOfAbbr(it)) }

            until?.also { until(Date(it)) }


        }.build()

    }

    private fun Cursor.toVEvent(): VEvent {

        return VEvent().apply {

            getString(Events.UID_2445)?.also { uid = Uid(it) }
            getLong(AEvents.REQURENCE_ID)?.also { recurrenceId = RecurrenceId(Date(it)) }

            lastModified = LastModified(Date())

            getString(Events.TITLE)?.also { summary = Summary(it) }
            getString(Events.DESCRIPTION)?.also { description = Description(it) }
            getString(Events.EVENT_LOCATION)?.also { location = Location(it) }

            val isAllDay = getLong(Events.ALL_DAY) == 1L

            val date = { timeColumn: String, tzColumn: String, hasTime: Boolean ->

                getLong(timeColumn)?.let {
                    val tz = getString(tzColumn)?.let { TimeZone.getTimeZone(it) } ?:
                            TimeZone.getTimeZone("UTC")
                    val dateComponents = DateTimeComponents(Date(it), tz)
                    ICalDate(dateComponents, hasTime)
                }

            }

            date(Events.DTSTART, Events.EVENT_TIMEZONE, !isAllDay)?.also {
                dateStart = DateStart(it)
            }

            date(Events.DTEND, Events.EVENT_END_TIMEZONE, !isAllDay)?.also {
                dateEnd = DateEnd(it)
            }

            getString(Events.DURATION)?.also {
                duration = DurationProperty(Duration.parse(it))
            }

            getString(Events.RRULE)
                    ?.let { parseRecurrence(it, getLong(Events.LAST_DATE)) }
                    ?.let { RecurrenceRule(it) }
                    ?.also { recurrenceRule = it }

            getString(Events.EXRULE)
                    ?.let { parseRecurrence(it) }
                    ?.let { ExceptionRule(it) }
                    ?.also { exceptionRules.add(it) }

            getString(Events.EXDATE)
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

    private fun Cursor.toAttendee(): Attendee? {

        val email = getString(Attendees.ATTENDEE_EMAIL) ?: return null
        val name = getString(Attendees.ATTENDEE_NAME)
                .takeIf { !it.isNullOrBlank() } ?: email

        return Attendee(name, email).apply {
            rsvp = true
            commonName = name
            uri = "mailto:$email"
        }

    }

    private fun Cursor.toAlarm(): VAlarm {

        val duration = (getLong(Reminders.MINUTES, true) ?: 0L)
                .let { Duration.fromMillis(-TimeUnit.MINUTES.toMillis(it)) }

        return VAlarm(Action.display(), Trigger(duration, null)).apply {
            description = Description("Alarm")
        }

    }

    private inline fun <T> Cursor.toList(mapper: (Cursor) -> T): List<T> {
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

    class Factory @Inject constructor(
            private val dao: CalendarsDao,
            private val calendarMapper: CalendarMapper,
            private val remoteServiceProvider: RemoteServiceProvider<CalendarRemoteService>
    ) {

        fun create(account: Account, client: ContentProviderClient) : CalendarsSyncOperation {
            return CalendarsSyncOperation(account, client, dao, calendarMapper, remoteServiceProvider)
        }

    }

    private val ContentProviderClient.calendars: ContentClientHelper
        get() = ContentClientHelper(this, getSyncUri(CalendarContract.Calendars.CONTENT_URI))

    private val ContentProviderClient.events: ContentClientHelper
        get() = ContentClientHelper(this, getSyncUri(Events.CONTENT_URI))

    private val ContentProviderClient.reminders: ContentClientHelper
        get() = ContentClientHelper(this, getSyncUri(Reminders.CONTENT_URI))

    private val ContentProviderClient.attendees: ContentClientHelper
        get() = ContentClientHelper(this, getSyncUri(Attendees.CONTENT_URI))

    private fun ContentValues.putall(vararg values: Pair<String, Any?>): ContentValues {

        values.filterNot { it.second == null }
                .forEach { (key, value) ->

                    when(value) {

                        is String -> put(key, value)
                        is Double -> put(key, value)
                        is Float -> put(key, value)
                        is Long -> put(key, value)
                        is Int -> put(key, value)
                        is Short -> put(key, value)
                        is Byte -> put(key, value)
                        is Boolean -> put(key, value)
                        is ByteArray -> put(key, value)

                    }

                }

        return this

    }

}