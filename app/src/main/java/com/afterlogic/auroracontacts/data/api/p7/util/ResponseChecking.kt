package com.afterlogic.auroracontacts.data.api.p7.util

import com.afterlogic.auroracontacts.data.api.error.ApiReturnedError
import com.afterlogic.auroracontacts.data.api.p7.model.ApiResponseP7
import io.reactivex.Single

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

fun <T> Single<ApiResponseP7<T>>.checkResponse(): Single<ApiResponseP7<T>> {
    return flatMap {
        if (it.isSuccess) Single.just(it)
        else Single.error(ApiReturnedError(it.errorCode ?: -1, it.errorMessage))
    }
}

fun <T, R> Single<ApiResponseP7<T>>.checkResponseAndGetData(
        mapper: (ApiResponseP7<T>) -> R
): Single<R> = checkResponse().map(mapper)

fun <T> Single<ApiResponseP7<T>>.checkResponseAndGetData(): Single<T> =
        checkResponse().map { it.data!! }
