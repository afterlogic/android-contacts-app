package com.afterlogic.auroracontacts.data.p7.api

import com.afterlogic.auroracontacts.data.api.Json
import com.afterlogic.auroracontacts.data.api.p7.util.Auth
import com.afterlogic.auroracontacts.data.api.p7.util.AuthValue
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.AJAX
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.Fields.ACCOUNT_ID
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.Fields.ACTION
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.Fields.AUTH_TOKEN
import com.afterlogic.auroracontacts.data.p7.api.ApiP7.Fields.TOKEN
import com.afterlogic.auroracontacts.data.p7.api.model.CalendarEventP7
import com.afterlogic.auroracontacts.data.p7.api.model.CalendarP7
import com.afterlogic.auroracontacts.data.p7.api.model.JsonList
import com.afterlogic.auroracontacts.data.p7.api.model.P7ApiResponse
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
            @Auth(AuthValue.APP_TOKEN) @Field(TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ): Single<P7ApiResponse<List<CalendarP7>>>

    @FormUrlEncoded
    @POST(AJAX)
    fun getEvents(
            @Json @Field("CalendarIds") calendarsIds: JsonList<String>,
            @Field("GetData") withData: BooleanInt,
            @Field(ACTION) action: String = "CalendarEventsInfo",
            @Auth(AuthValue.APP_TOKEN) @Field(TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ): Single<P7ApiResponse<Map<String, List<CalendarEventP7>>>>

    @FormUrlEncoded
    @POST(AJAX)
    fun updateEvent(
            @Field("calendarId") calendarId: String,
            @Field("url") url: String?,
            @Field("data") rawIcsData: String,
            @Field(ACTION) action: String = "CalendarEventUpdateRaw",
            @Auth(AuthValue.APP_TOKEN) @Field(TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<Boolean>>

    @FormUrlEncoded
    @POST(AJAX)
    fun deleteEvents(
            @Field("calendarId") calendarId: String,
            @Json @Field("eventUrls") urls: JsonList<String>,
            @Field(ACTION) action: String = "CalendarEventsDeleteByUrls",
            @Auth(AuthValue.APP_TOKEN) @Field(TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<Boolean>>

}