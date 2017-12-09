package com.afterlogic.auroracontacts.data.calendar

import com.afterlogic.auroracontacts.data.LocalDataNotExistsError
import com.afterlogic.auroracontacts.data.NotSupportedApiError
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedException
import com.afterlogic.auroracontacts.data.db.CalendarDbe
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.data.db.SyncSettings
import com.afterlogic.auroracontacts.data.db.plusAssign
import com.afterlogic.auroracontacts.data.p7.calendars.CalendarsRepositoryP7
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
        private val calendarsDao: CalendarsDao,
        private val p7Repository: CalendarsRepositoryP7,
        private val mapper: CalendarMapper
) {

    enum class Source { LOCAL, REMOTE, BOTH }

    private val remoteService: Single<CalendarRemoteService> get() = accountService.accountSession
                    .firstOrError()
                    .map {

                        val session = it.get() ?: throw UserNotAuthorizedException()
                        val apiType = ApiType.byCode(session.apiVersion) ?:
                                throw NotSupportedApiError()

                        when(apiType) {
                            ApiType.P7 -> p7Repository
                            else -> throw NotSupportedApiError()
                        }

                    }

    fun getCalendars(source: Source = Source.BOTH): Flowable<List<AuroraCalendar>> {

        val local: () -> Flowable<List<AuroraCalendar>> = {

            calendarsDao.all.map { it.map(mapper::toPlain) }
                    .startWith(checkLocalDataExist { prefs.calendarsFetched })

        }

        val remote: () -> Flowable<List<AuroraCalendar>> = {

            remoteService.flatMap { it.getCalendars() }
                    .map { it.map { mapper.toDbe(it) } }
                    .doOnSuccess {
                        prefs.calendarsFetched = true
                        calendarsDao += it
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

    fun getEvents(calendarId: String): Single<List<AuroraCalendarEvent>> {
        return remoteService.flatMap { it.getEvents(calendarId) }
    }

    fun updateEvent(event: AuroraCalendarEvent): Completable {
        return remoteService.flatMapCompletable { it.updateEvent(event) }
    }

    fun deleteEvent(event: AuroraCalendarEvent): Completable {
        return remoteService.flatMapCompletable { it.deleteEvent(event) }
    }

    private fun <T> checkLocalDataExist(check: () -> Boolean): Flowable<T> {

        return Completable.fromAction {
            if (!check()) throw LocalDataNotExistsError()
        }.toFlowable()

    }

}

interface CalendarRemoteService {
    fun getCalendars(): Single<List<AuroraCalendar>>
    fun getEvents(calendarId: String): Single<List<AuroraCalendarEvent>>
    fun updateEvent(event: AuroraCalendarEvent): Completable
    fun deleteEvent(event: AuroraCalendarEvent): Completable
}

class CalendarMapper @Inject constructor() {

    fun toDbe(source: AuroraCalendar, syncEnabled: Boolean = false): CalendarDbe {

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
        return AuroraCalendar(
                souce.id,
                souce.name,
                souce.description,
                souce.color
        )
    }

}