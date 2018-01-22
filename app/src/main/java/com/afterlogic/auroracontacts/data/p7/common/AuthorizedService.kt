package com.afterlogic.auroracontacts.data.p7.common

import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedError
import com.afterlogic.auroracontacts.data.api.p7.util.AuthConverterFactoryP7
import com.afterlogic.auroracontacts.data.p7.api.DynamicLazyApiP7
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
open class AuthorizedService<T> @Inject constructor(
        private val dynamicLazyApiP7: DynamicLazyApiP7<T>,
        private val accountService: AccountService,
        private val authConverterFactory: AuthConverterFactoryP7
) {

    protected val api: Single<T> get() = accountService.accountSession
            .observeOn(Schedulers.io())
            .firstElement()
            .filter { it.get() != null }
            .map { it.get() }
            .switchIfEmpty(Maybe.error(UserNotAuthorizedError()))
            .toSingle()
            // TODO: better approach?
            .doOnSuccess { authConverterFactory.currentSession = it }
            .map { dynamicLazyApiP7[it.domain] }

}