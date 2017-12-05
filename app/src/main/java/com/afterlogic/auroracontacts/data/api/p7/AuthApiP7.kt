package com.afterlogic.auroracontacts.data.api.p7

import com.afterlogic.auroracontacts.data.api.p7.ApiP7.AJAX
import com.afterlogic.auroracontacts.data.api.p7.ApiP7.Actions.SYSTEM_GET_APP_DATA
import com.afterlogic.auroracontacts.data.api.p7.ApiP7.Actions.SYSTEM_LOGIN
import com.afterlogic.auroracontacts.data.api.p7.ApiP7.Fields.ACTION
import com.afterlogic.auroracontacts.data.api.p7.ApiP7.Fields.AUTH_TOKEN
import com.afterlogic.auroracontacts.data.api.p7.ApiP7.Fields.EMAIL
import com.afterlogic.auroracontacts.data.api.p7.ApiP7.Fields.INC_PASSWORD
import com.afterlogic.auroracontacts.data.api.p7.model.ApiResponseP7
import com.afterlogic.auroracontacts.data.api.p7.model.AuthTokenP7
import com.afterlogic.auroracontacts.data.api.p7.model.SystemAppDataP7
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
    ): Single<ApiResponseP7<SystemAppDataP7>>

    @FormUrlEncoded
    @POST(AJAX)
    fun login(
            @Field(EMAIL) email: String,
            @Field(INC_PASSWORD) password: String,
            @Field(ACTION) action: String = SYSTEM_LOGIN
    ): Single<ApiResponseP7<AuthTokenP7>>

}