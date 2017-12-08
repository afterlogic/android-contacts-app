package com.afterlogic.auroracontacts.data

import com.afterlogic.auroracontacts.data.api.ApiType

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

class NotSupportedApiError(val api: ApiType = ApiType.UNKNOWN, message: String? = null):
        Throwable(message ?: "Not supported api.")

class IllegalApiDataException(message: String): Exception(message)