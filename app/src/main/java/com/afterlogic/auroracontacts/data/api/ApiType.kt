package com.afterlogic.auroracontacts.data.api

import dagger.MapKey

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

@MapKey
annotation class ApiTypeKey(val value: ApiType)

enum class ApiType(val code: Int) {
    UNKNOWN(-1), P7(7000), P8(8000);

    companion object {

        fun byCode(code: Int): ApiType? = ApiType.values().firstOrNull { code == it.code }

        val supportedApiTypes: Array<ApiType> = arrayOf(P7)

    }

}