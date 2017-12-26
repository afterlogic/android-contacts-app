package com.afterlogic.auroracontacts.data.p7.api

import com.afterlogic.auroracontacts.data.api.p7.util.Auth
import com.afterlogic.auroracontacts.data.api.p7.util.AuthValue
import com.afterlogic.auroracontacts.data.p7.api.model.ApiResponseP7
import com.afterlogic.auroracontacts.data.p7.api.model.ContactsGroupP7
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

interface ContactsApiP7 {

    @FormUrlEncoded
    @POST(ApiP7.AJAX)
    fun getContactsGroups(
            @Field(ApiP7.Fields.ACTION) action: String = "ContactsGroupFullList",
            @Auth(AuthValue.APP_TOKEN) @Field(ApiP7.Fields.TOKEN) appToken: String = AuthValue.STRING,
            @Auth(AuthValue.AUTH_TOKEN) @Field(ApiP7.Fields.AUTH_TOKEN) authToken: String = AuthValue.STRING,
            @Auth(AuthValue.ACCOUNT_ID) @Field(ApiP7.Fields.ACCOUNT_ID) accountId: Long = AuthValue.LONG
    ) : Single<ApiResponseP7<List<ContactsGroupP7>>>

}