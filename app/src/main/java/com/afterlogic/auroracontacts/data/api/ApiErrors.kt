package com.afterlogic.auroracontacts.data.api

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

open class ApiError(message: String? = null): Throwable(message)

class ApiReturnedError(val code: Int, val apiType: ApiType, message: String?): ApiError(message)

class UserNotAuthorizedException: ApiError("User not authorized.")