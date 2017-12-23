package com.afterlogic.auroracontacts.data.p7.calendars

import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.p7.api.model.CalendarEventP7
import com.afterlogic.auroracontacts.data.p7.api.model.CalendarP7
import com.afterlogic.auroracontacts.data.api.p7.util.AuthConverterFactoryP7
import com.afterlogic.auroracontacts.data.api.p7.util.checkResponseAndGetData
import com.afterlogic.auroracontacts.data.p7.api.CalendarApiP7
import com.afterlogic.auroracontacts.data.p7.api.DynamicLazyApiP7
import com.afterlogic.auroracontacts.data.p7.api.model.JsonList
import com.afterlogic.auroracontacts.data.p7.common.AuthorizedService
import com.afterlogic.auroracontacts.data.util.BooleanInt
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CalendarsCloudServiceP7 @Inject constructor(
        dynamicLazyApiP7: DynamicLazyApiP7<CalendarApiP7>,
        accountService: AccountService,
        authConverterFactoryP7: AuthConverterFactoryP7
) : AuthorizedService<CalendarApiP7>(dynamicLazyApiP7, accountService, authConverterFactoryP7) {

    fun getCalendars(): Single<List<CalendarP7>> = api.flatMap { it.getCalendars() }
            .checkResponseAndGetData()

    fun getCalendarEvents(calendarId: String) : Single<List<CalendarEventP7>> = api
            .flatMap { it.getEvents(JsonList(listOf(calendarId)), BooleanInt(true)) }
            .checkResponseAndGetData { it.data!![calendarId] ?: emptyList() }

    fun updateEvent(calendarId: String, eventUrl: String?, data: String): Single<Boolean> =
            api.flatMap { it.updateEvent(calendarId, eventUrl, data) }
                    .checkResponseAndGetData()

    fun deleteEvent(calendarId: String, urls: List<String>): Single<Boolean> =
            api.flatMap { it.deleteEvents(calendarId, JsonList(urls)) }
                    .checkResponseAndGetData()

}