package com.afterlogic.auroracontacts.data.p7.calendars

import android.graphics.Color
import com.afterlogic.auroracontacts.data.api.p7.model.CalendarEventP7
import com.afterlogic.auroracontacts.data.api.p7.model.CalendarP7
import com.afterlogic.auroracontacts.data.calendar.AuroraCalendar
import com.afterlogic.auroracontacts.data.calendar.AuroraCalendarEvent
import com.afterlogic.auroracontacts.data.calendar.CalendarRemoteService
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CalendarsRepositoryP7 @Inject constructor(
        private val cloudService: CalendarsCloudServiceP7
) : CalendarRemoteService {

    override fun getCalendars(): Single<List<AuroraCalendar>> = cloudService.getCalendars()
            .map { it.map(this::parseCalendar) }

    override fun getEvents(calendarId: String): Single<List<AuroraCalendarEvent>> =
            cloudService.getCalendarEvents(calendarId)
                    .map { it.map(this::parseEvent) }

    override fun updateEvent(event: AuroraCalendarEvent): Completable {
        TODO("not implemented") //To change body of created functions use File | SyncSettings | File Templates.
    }

    override fun deleteEvent(event: AuroraCalendarEvent): Completable {
        TODO("not implemented") //To change body of created functions use File | SyncSettings | File Templates.
    }

    private fun parseCalendar(dto: CalendarP7) : AuroraCalendar = AuroraCalendar(
            dto.id, dto.name, dto.description, Color.parseColor(dto.color)
    )

    private fun parseEvent(dto: CalendarEventP7) : AuroraCalendarEvent = AuroraCalendarEvent(dto.url)

}