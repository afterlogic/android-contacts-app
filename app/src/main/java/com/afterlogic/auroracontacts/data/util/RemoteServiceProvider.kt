package com.afterlogic.auroracontacts.data.util

import com.afterlogic.auroracontacts.data.NotSupportedApiError
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedException
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Provider

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

class RemoteServiceProvider<T> @Inject constructor(
        private val accountService: AccountService,
        private val services: Map<ApiType, @JvmSuppressWildcards Provider<@JvmSuppressWildcards T>>
) {

    fun get(): Single<T> = accountService.accountSession
            .firstOrError()
            .map {

                val session = it.get() ?: throw UserNotAuthorizedException()
                val apiType = ApiType.byCode(session.apiVersion) ?: throw NotSupportedApiError()

                services[apiType]?.get() ?: throw NotSupportedApiError()

            }

}