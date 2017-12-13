package com.afterlogic.auroracontacts.data.api.p7.util

import com.afterlogic.auroracontacts.data.AuthFailedError
import com.afterlogic.auroracontacts.data.api.ApiReturnedError
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.p7.model.ApiResponseP7
import io.reactivex.Single

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

fun <T> Single<ApiResponseP7<T>>.checkResponse(): Single<ApiResponseP7<T>> {

    return flatMap {

        if (it.isSuccess) Single.just(it)
        else {

            val error = ApiReturnedError(it.errorCode ?: -1, ApiType.P7, it.errorMessage)

            val resultError = when(error.code) {
                102 -> AuthFailedError(error)
                else -> error
            }

            Single.error(resultError)
        }

    }

}

fun <T, R> Single<ApiResponseP7<T>>.checkResponseAndGetData(
        mapper: (ApiResponseP7<T>) -> R
): Single<R> = checkResponse().map(mapper)

fun <T> Single<ApiResponseP7<T>>.checkResponseAndGetData(): Single<T> =
        checkResponseAndGetData { it.data!! }
