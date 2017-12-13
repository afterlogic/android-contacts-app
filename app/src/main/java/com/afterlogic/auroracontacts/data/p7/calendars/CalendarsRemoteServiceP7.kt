package com.afterlogic.auroracontacts.data.p7.calendars

import android.graphics.Color
import com.afterlogic.auroracontacts.data.api.p7.model.CalendarEventP7
import com.afterlogic.auroracontacts.data.api.p7.model.CalendarP7
import com.afterlogic.auroracontacts.data.auth.AuthResolver
import com.afterlogic.auroracontacts.data.calendar.*
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CalendarsRemoteServiceP7 @Inject constructor(
        private val authResolver: AuthResolver,
        private val cloudService: CalendarsCloudServiceP7
) : CalendarRemoteService {

    override fun getCalendars(): Single<List<RemoteCalendar>> = cloudService.getCalendars()
            .map { it.map(this::parseCalendar) }
            .retryWhen(authResolver.checkAndResolveAuth)

    override fun getEvents(calendarId: String): Single<List<RemoteCalendarEvent>> =
            cloudService.getCalendarEvents(calendarId)
                    .map { it.map(this::parseEvent) }
                    .retryWhen(authResolver.checkAndResolveAuth)

    override fun updateEvent(request: UpdateCalendarEventRequest): Completable =
            cloudService.updateEvent(request.calendarId, request.id, request.icsData)
                    .toCompletable()
                    .retryWhen(authResolver.checkAndResolveAuth)

    override fun deleteEvent(request: DeleteCalendarEventsRequest): Completable =
            cloudService.deleteEvent(request.calendarId, request.ids)
                    .toCompletable()
                    .retryWhen(authResolver.checkAndResolveAuth)

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

    private fun parseEvent(dto: CalendarEventP7) : RemoteCalendarEvent = RemoteCalendarEvent(
            dto.url,
            dto.lastModified.toLong(),
            dto.eTag,
            dto.data!!
    )

}