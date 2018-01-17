package com.afterlogic.auroracontacts.data.p7.api

import com.afterlogic.auroracontacts.data.api.p7.util.Auth
import com.afterlogic.auroracontacts.data.api.p7.util.AuthValue
import com.afterlogic.auroracontacts.data.p7.api.model.P7ApiResponse
import com.afterlogic.auroracontacts.data.p7.api.model.P7CreateContactResult
import com.afterlogic.auroracontacts.data.p7.contacts.P7ContactsData
import com.afterlogic.auroracontacts.data.p7.contacts.P7RemoteContact
import com.afterlogic.auroracontacts.data.p7.contacts.P7RemoteFullContact
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FieldMap
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
    @POST(P7Api.AJAX)
    fun getContactsGroups(
            @Field(P7Api.Fields.ACTION) action: String = "ContactsGroupFullList",
            @Auth(AuthValue.APP_TOKEN) @Field(P7Api.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(P7Api.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(P7Api.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<List<P7RemoteContact>>>

    @FormUrlEncoded
    @POST(P7Api.AJAX)
    fun getContacts(
            @Field(P7Api.Fields.ACTION) action: String,
            @Field("Limit") limit: Int,
            @Field("Offset") offset: Int,
            @Field("All") all: Int?,
            @Field("SharedToAll") sharedToAll: Int?,
            @Field("SharedToAll1") sharedToAll1: Int?,
            @Field("GroupId") groupIdField: Long?,
            @Field("SortField") sortField: String = "Name",
            @Field("SortOrder") sortOrder: Int = 1,
            @Auth(AuthValue.APP_TOKEN) @Field(P7Api.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(P7Api.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(P7Api.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<P7ContactsData>>

    @FormUrlEncoded
    @POST(P7Api.AJAX)
    fun getFullContact(
            @Field("ContactId") id: Long,
            @Field(P7Api.Fields.ACTION) action: String = "ContactGet",
            @Auth(AuthValue.APP_TOKEN) @Field(P7Api.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(P7Api.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(P7Api.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<P7RemoteFullContact>>

    @FormUrlEncoded
    @POST(P7Api.AJAX)
    fun createContact(
            @FieldMap contact: Map<String, @JvmSuppressWildcards Any?>,
            @Field(P7Api.Fields.ACTION) action: String = "ContactCreate",
            @Auth(AuthValue.APP_TOKEN) @Field(P7Api.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(P7Api.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(P7Api.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<P7CreateContactResult>>

    @FormUrlEncoded
    @POST(P7Api.AJAX)
    fun updateContact(
            @Field("ContactId") id: Long,
            @Field(value = "GroupsIds[]", encoded = true) groupIds: List<String>?,
            @FieldMap contact: Map<String, @JvmSuppressWildcards Any?>,
            @Field(P7Api.Fields.ACTION) action: String = "ContactUpdate",
            @Auth(AuthValue.APP_TOKEN) @Field(P7Api.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(P7Api.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(P7Api.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<P7ApiResponse<Boolean>>

}