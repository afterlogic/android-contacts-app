package com.afterlogic.auroracontacts.data.p7.api

import com.afterlogic.auroracontacts.data.api.p7.util.Auth
import com.afterlogic.auroracontacts.data.api.p7.util.AuthValue
import com.afterlogic.auroracontacts.data.p7.api.model.P7ApiResponse
import com.afterlogic.auroracontacts.data.p7.contacts.P7ContactsData
import com.afterlogic.auroracontacts.data.p7.contacts.P7RemoteContact
import com.afterlogic.auroracontacts.data.p7.contacts.P7RemoteFullContact
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

interface P7ContactsApi {

    @FormUrlEncoded
    @POST(ApiP7.AJAX)
    fun getContactsGroups(
            @Field(ApiP7.Fields.ACTION) action: String = "ContactsGroupFullList",
            @Auth(AuthValue.APP_TOKEN) @Field(ApiP7.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(ApiP7.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(ApiP7.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<List<P7RemoteContact>>>

    @FormUrlEncoded
    @POST(ApiP7.AJAX)
    fun getContacts(
            @Field(ApiP7.Fields.ACTION) action: String,
            @Field("Limit") limit: Int,
            @Field("Offset") offset: Int,
            @Field("All") all: Int?,
            @Field("SharedToAll") sharedToAll: Int?,
            @Field("SharedToAll1") sharedToAll1: Int?,
            @Field("GroupId") groupIdField: Long?,
            @Field("SortField") sortField: String = "Name",
            @Field("SortOrder") sortOrder: Int = 1,
            @Auth(AuthValue.APP_TOKEN) @Field(ApiP7.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(ApiP7.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(ApiP7.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<P7ContactsData>>


    @FormUrlEncoded
    @POST(ApiP7.AJAX)
    fun getFullContact(
            @Field("ContactId") id: Long,
            @Field(ApiP7.Fields.ACTION) action: String = "ContactGet",
            @Auth(AuthValue.APP_TOKEN) @Field(ApiP7.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(ApiP7.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(ApiP7.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<P7RemoteFullContact>>

}