package com.afterlogic.auroracontacts.data.api.error

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

class ApiReturnedError(val code: Int, message: String?): Throwable(message)

class UserNotAuthorizedException(): Throwable("User not authorized.")