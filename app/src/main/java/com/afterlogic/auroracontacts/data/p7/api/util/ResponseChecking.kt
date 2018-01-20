package com.afterlogic.auroracontacts.data.api.p7.util

import com.afterlogic.auroracontacts.data.AuthFailedError
import com.afterlogic.auroracontacts.data.api.ApiReturnedError
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.ApiNullResultError
import com.afterlogic.auroracontacts.data.p7.api.model.P7ApiResponse
import io.reactivex.Single

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

fun <T> Single<P7ApiResponse<T>>.checkResponse(): Single<P7ApiResponse<T>> {

    return flatMap {

        if (it.isSuccess) Single.just(it)
        else {

            if (it.data == null && it.errorCode == null && it.errorMessage == null) {
                return@flatMap Single.error<P7ApiResponse<T>>(ApiNullResultError())
            }

            val error = ApiReturnedError(it.errorCode ?: -1, ApiType.P7, it.errorMessage)

            val resultError = when(error.code) {
                102 -> AuthFailedError(error)
                else -> error
            }

            Single.error(resultError)

        }

    }

}

fun <T, R> Single<P7ApiResponse<T>>.checkResponseAndGetData(
        mapper: (P7ApiResponse<T>) -> R
): Single<R> = checkResponse().map(mapper)

fun <T> Single<P7ApiResponse<T>>.checkResponseAndGetData(): Single<T> =
        checkResponseAndGetData { it.data!! }
