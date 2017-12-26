package com.afterlogic.auroracontacts.data.calendar

import io.reactivex.Completable
import io.reactivex.Single

interface CalendarRemoteService {
    fun getCalendars(): Single<List<RemoteCalendar>>
    fun getEvents(calendarId: String): Single<List<RemoteCalendarEvent>>
    fun updateEvent(request: UpdateCalendarEventRequest): Completable
    fun deleteEvent(request: DeleteCalendarEventsRequest): Completable
}