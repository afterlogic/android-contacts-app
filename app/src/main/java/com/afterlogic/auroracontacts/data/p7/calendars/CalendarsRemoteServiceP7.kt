package com.afterlogic.auroracontacts.data.p7.calendars

import android.graphics.Color
import com.afterlogic.auroracontacts.data.api.p7.model.CalendarEventP7
import com.afterlogic.auroracontacts.data.api.p7.model.CalendarP7
import com.afterlogic.auroracontacts.data.calendar.CalendarRemoteService
import com.afterlogic.auroracontacts.data.calendar.RemoteCalendar
import com.afterlogic.auroracontacts.data.calendar.RemoteCalendarEvent
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CalendarsRemoteServiceP7 @Inject constructor(
        private val cloudService: CalendarsCloudServiceP7
) : CalendarRemoteService {

    override fun getCalendars(): Single<List<RemoteCalendar>> = cloudService.getCalendars()
            .map { it.map(this::parseCalendar) }

    override fun getEvents(calendarId: String): Single<List<RemoteCalendarEvent>> =
            cloudService.getCalendarEvents(calendarId)
                    .map { it.map(this::parseEvent) }

    override fun updateEvent(event: RemoteCalendarEvent): Completable {
        TODO("not implemented") //To change body of created functions use File | SyncSettings | File Templates.
    }

    override fun deleteEvent(event: RemoteCalendarEvent): Completable {
        TODO("not implemented") //To change body of created functions use File | SyncSettings | File Templates.
    }

    private fun parseCalendar(dto: CalendarP7) : RemoteCalendar = RemoteCalendar(
            dto.id,
            dto.name,
            dto.description,
            Color.parseColor(dto.color),
            dto.eTag,
            dto.cTag,
            dto.owner,
            if (dto.access == 1) RemoteCalendar.AccessLevel.EDITOR
            else RemoteCalendar.AccessLevel.READ
    )

    private fun parseEvent(dto: CalendarEventP7) : RemoteCalendarEvent = RemoteCalendarEvent(dto.url)

}