package com.afterlogic.auroracontacts.data.calendar

import com.afterlogic.auroracontacts.data.p7.calendars.CalendarsRepositoryP7
import com.afterlogic.auroracontacts.presentation.AppScope
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

interface CalendarSubRepository {
    fun getCalendars(): Single<List<AuroraCalendar>>
    fun getEvents(calendarId: String): Single<List<AuroraCalendarEvent>>
    fun updateEvent(event: AuroraCalendarEvent): Completable
    fun deleteEvent(event: AuroraCalendarEvent): Completable
}

@AppScope
class CalendarsRepository @Inject constructor(
        private val p7Repository: CalendarsRepositoryP7
) : CalendarSubRepository {

    override fun getCalendars(): Single<List<AuroraCalendar>> {
        return p7Repository.getCalendars()
    }

    override fun getEvents(calendarId: String): Single<List<AuroraCalendarEvent>> {
        return p7Repository.getEvents(calendarId)
    }

    override fun updateEvent(event: AuroraCalendarEvent): Completable {
        return p7Repository.updateEvent(event)
    }

    override fun deleteEvent(event: AuroraCalendarEvent): Completable {
        return p7Repository.deleteEvent(event)
    }

}