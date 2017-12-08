package com.afterlogic.auroracontacts.data.p7.api

import com.afterlogic.auroracontacts.data.api.p7.model.ApiResponseP7
import com.afterlogic.auroracontacts.data.api.p7.model.CalendarEventP7
import com.afterlogic.auroracontacts.data.api.p7.model.CalendarP7
import com.afterlogic.auroracontacts.data.api.p7.util.Auth
import com.afterlogic.auroracontacts.data.api.p7.util.AuthValue
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.AJAX
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.Fields.ACCOUNT_ID
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.Fields.ACTION
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.Fields.AUTH_TOKEN
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.Fields.TOKEN
import com.afterlogic.auroracontacts.data.util.BooleanInt
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by sashka on 10.10.16.
 *
 *
 * mail: sunnyday.development@gmail.com
 */

interface CalendarApiP7 {

    @FormUrlEncoded
    @POST(AJAX)
    fun getCalendars(
            @Field(ACTION) action: String = "CalendarList",
            @Auth @Field(TOKEN) appToken: String? = AuthValue.APP_TOKEN,
            @Auth @Field(AUTH_TOKEN) authToken: String? = AuthValue.AUTH_TOKEN,
            @Auth @Field(ACCOUNT_ID) accountId: Long? = AuthValue.ACCOUNT_ID
    ): Single<ApiResponseP7<List<CalendarP7>>>

    @FormUrlEncoded
    @POST(AJAX)
    fun getEvents(
            @Field("CalendarIds") calendarsIds: List<String>,
            @Field("GetData") withData: BooleanInt,
            @Field(ACTION) action: String = "CalendarEventsInfo",
            @Auth @Field(TOKEN) appToken: String? = AuthValue.APP_TOKEN,
            @Auth @Field(AUTH_TOKEN) authToken: String? = AuthValue.AUTH_TOKEN,
            @Auth @Field(ACCOUNT_ID) accountId: Long? = AuthValue.ACCOUNT_ID
    ): Single<ApiResponseP7<Map<String, List<CalendarEventP7>>>>

    @FormUrlEncoded
    @POST(AJAX)
    fun updateEvent(
            @Field("calendarId") calendarId: String,
            @Field("url") url: String,
            @Field("data") rawIcsData: String,
            @Field(ACTION) action: String = "CalendarEventUpdateRaw",
            @Auth @Field(TOKEN) appToken: String? = AuthValue.APP_TOKEN,
            @Auth @Field(AUTH_TOKEN) authToken: String? = AuthValue.AUTH_TOKEN,
            @Auth @Field(ACCOUNT_ID) accountId: Long? = AuthValue.ACCOUNT_ID
    ) : Single<ApiResponseP7<Boolean>>

    @FormUrlEncoded
    @POST(AJAX)
    fun deleteEvents(
            @Field("calendarId") calendarId: String,
            @Field("eventUrls") urls: List<String>,
            @Field(ACTION) action: String = "CalendarEventsDeleteByUrls",
            @Auth @Field(TOKEN) appToken: String? = AuthValue.APP_TOKEN,
            @Auth @Field(AUTH_TOKEN) authToken: String? = AuthValue.AUTH_TOKEN,
            @Auth @Field(ACCOUNT_ID) accountId: Long? = AuthValue.ACCOUNT_ID
    ) : Single<ApiResponseP7<Boolean>>

}