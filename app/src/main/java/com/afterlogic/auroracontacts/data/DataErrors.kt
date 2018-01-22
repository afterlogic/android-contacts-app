package com.afterlogic.auroracontacts.data

import com.afterlogic.auroracontacts.data.api.ApiType

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

class UnsupportedApiError(val api: ApiType = ApiType.UNKNOWN, message: String? = null):
        Throwable(message ?: "Not supported api.")

class IllegalApiDataException(message: String): Exception(message)

class LocalDataNotExistsError: Throwable()

class AuthFailedError(cause: Throwable? = null): Throwable(cause) {

    var checked: Boolean = false
        private set

    fun markChecked() {
        checked = true
    }

}