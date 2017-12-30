package com.afterlogic.auroracontacts.data.api

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

open class ApiError(message: String? = null): Throwable(message)

open class ApiReturnedError(val code: Int? = null, val apiType: ApiType = ApiType.UNKNOWN, message: String? = null): ApiError(message)

class ApiNullResultError(): ApiReturnedError(message = "Api returned null result.")

class UserNotAuthorizedException: ApiError("User not authorized.")