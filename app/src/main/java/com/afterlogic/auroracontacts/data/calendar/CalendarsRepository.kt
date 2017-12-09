package com.afterlogic.auroracontacts.data.calendar

import com.afterlogic.auroracontacts.data.LocalDataNotExistsError
import com.afterlogic.auroracontacts.data.NotSupportedApiError
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedException
import com.afterlogic.auroracontacts.data.db.CalendarDbe
import com.afterlogic.auroracontacts.data.db.CalendarEventDbe
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.data.db.SyncSettings
import com.afterlogic.auroracontacts.data.p7.calendars.CalendarsRemoteServiceP7
import com.afterlogic.auroracontacts.data.preferences.Prefs
import com.afterlogic.auroracontacts.presentation.AppScope
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class CalendarsRepository @Inject constructor(
        private val prefs: Prefs,
        private val accountService: AccountService,
        private val dao: CalendarsDao,
        private val p7RemoteService: CalendarsRemoteServiceP7,
        private val calendarMapper: CalendarMapper,
        private val eventsMapper: EventsMapper
) {

    enum class Source { LOCAL, REMOTE, BOTH }

    private val remoteService: Single<CalendarRemoteService> get() = accountService.accountSession
                    .firstOrError()
                    .map {

                        val session = it.get() ?: throw UserNotAuthorizedException()
                        val apiType = ApiType.byCode(session.apiVersion) ?:
                                throw NotSupportedApiError()

                        when(apiType) {
                            ApiType.P7 -> p7RemoteService
                            else -> throw NotSupportedApiError()
                        }

                    }

    //region// Calendars

    fun getCalendars(source: Source = Source.BOTH): Flowable<List<AuroraCalendar>> {

        val local: () -> Flowable<List<AuroraCalendar>> = {

            dao.all.map { it.map(calendarMapper::toPlain) }
                    .startWith(checkLocalDataExist { prefs.calendarsFetched })

        }

        val remote: () -> Flowable<List<AuroraCalendar>> = {

            remoteService.flatMap { it.getCalendars() }
                    .map { it.map { calendarMapper.toDbe(it) } }
                    .doOnSuccess {
                        prefs.calendarsFetched = true
                        dao += it
                    }
                    .toCompletable()
                    .toFlowable<List<AuroraCalendar>>()
                    .concatWith(local())

        }

        return when(source) {

            Source.LOCAL -> local()

            Source.REMOTE -> remote()

            Source.BOTH -> local()
                    .onErrorResumeNext(Flowable.empty())
                    .firstElement()
                    .toFlowable()
                    .concatWith(remote())

        }

    }

    fun setSyncEnabled(calendar: AuroraCalendar, enabled: Boolean): Completable =
            Completable.fromAction { dao.setSyncEnabled(calendar.id, enabled) }

    //endregion

    fun getEvents(calendarId: String): Single<List<AuroraCalendarEvent>> {
        return remoteService.flatMap { it.getEvents(calendarId) }
                .map { TODO() }
    }

    fun updateEvent(event: AuroraCalendarEvent): Completable {
        return remoteService.flatMapCompletable { it.updateEvent(eventsMapper.toRemote(event)) }
    }

    fun deleteEvent(event: AuroraCalendarEvent): Completable {
        return remoteService.flatMapCompletable { it.deleteEvent(eventsMapper.toRemote(event)) }
    }

    private fun <T> checkLocalDataExist(check: () -> Boolean): Flowable<T> {

        return Completable.fromAction {
            if (!check()) throw LocalDataNotExistsError()
        }.toFlowable()

    }

}

interface CalendarRemoteService {
    fun getCalendars(): Single<List<RemoteCalendar>>
    fun getEvents(calendarId: String): Single<List<RemoteCalendarEvent>>
    fun updateEvent(event: RemoteCalendarEvent): Completable
    fun deleteEvent(event: RemoteCalendarEvent): Completable
}

class CalendarMapper @Inject constructor() {

    fun toDbe(source: RemoteCalendar, syncEnabled: Boolean = false): CalendarDbe {

        val settings = SyncSettings(syncEnabled)

        return CalendarDbe(
                source.id,
                source.name,
                source.description,
                source.color,
                settings
        )

    }

    fun toPlain(souce: CalendarDbe): AuroraCalendar {

        val settings = AuroraCalendarSettings(
                souce.settings.syncEnabled
        )

        return AuroraCalendar(
                souce.id,
                souce.name,
                souce.description,
                souce.color,
                settings
        )
    }

}

class EventsMapper @Inject constructor() {

    fun toDbe(remote: RemoteCalendarEvent): CalendarEventDbe {
        TODO()
    }

    fun toPlain(dbe: CalendarEventDbe): AuroraCalendarEvent {
        TODO()
    }

    fun toRemote(plain: AuroraCalendarEvent): RemoteCalendarEvent {
        TODO()
    }

}