package com.afterlogic.auroracontacts.data.api.p7

import com.afterlogic.auroracontacts.data.api.p7.ApiP7.Companion.AJAX
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

interface AuthApiP7 : ApiP7 {

    @FormUrlEncoded
    @POST(AJAX)
    fun getSystemAppData(
            @Field(ApiP7.Fields.AUTH_TOKEN) token: String? = null,
            @Field(ApiP7.Fields.ACTION) action: String = ApiP7.Actions.SYSTEM_GET_APP_DATA
    ): Single<ApiResponseP7<SystemAppDataP7>>

    @FormUrlEncoded
    @POST(AJAX)
    fun login(
            @Field(ApiP7.Fields.EMAIL) email: String,
            @Field(ApiP7.Fields.INC_PASSWORD) password: String,
            @Field(ApiP7.Fields.ACTION) action: String = ApiP7.Actions.SYSTEM_LOGIN
    ): Single<ApiResponseP7<AuthTokenP7>>

}