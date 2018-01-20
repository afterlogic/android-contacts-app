package com.afterlogic.auroracontacts.data.p7.api

import com.afterlogic.auroracontacts.data.api.p7.model.AuthTokenP7
import com.afterlogic.auroracontacts.data.api.p7.model.SystemAppDataP7
import com.afterlogic.auroracontacts.data.p7.api.P7Api.AJAX
import com.afterlogic.auroracontacts.data.p7.api.P7Api.Actions.SYSTEM_GET_APP_DATA
import com.afterlogic.auroracontacts.data.p7.api.P7Api.Actions.SYSTEM_LOGIN
import com.afterlogic.auroracontacts.data.p7.api.P7Api.Fields.ACTION
import com.afterlogic.auroracontacts.data.p7.api.P7Api.Fields.AUTH_TOKEN
import com.afterlogic.auroracontacts.data.p7.api.P7Api.Fields.EMAIL
import com.afterlogic.auroracontacts.data.p7.api.P7Api.Fields.INC_PASSWORD
import com.afterlogic.auroracontacts.data.p7.api.model.P7ApiResponse
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

interface AuthApiP7 {

    @FormUrlEncoded
    @POST(AJAX)
    fun getSystemAppData(
            @Field(AUTH_TOKEN) token: String? = null,
            @Field(ACTION) action: String = SYSTEM_GET_APP_DATA
    ): Single<P7ApiResponse<SystemAppDataP7>>

    @FormUrlEncoded
    @POST(AJAX)
    fun login(
            @Field(EMAIL) email: String,
            @Field(INC_PASSWORD) password: String,
            @Field(ACTION) action: String = SYSTEM_LOGIN
    ): Single<P7ApiResponse<AuthTokenP7>>

}