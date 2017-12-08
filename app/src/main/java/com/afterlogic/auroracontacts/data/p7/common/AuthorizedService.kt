package com.afterlogic.auroracontacts.data.p7.common

import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.p7.api.DynamicLazyApiP7
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
open class AuthorizedService<T> @Inject constructor(
        private val dynamicLazyApiP7: DynamicLazyApiP7<T>,
        private val accountService: AccountService
) {

    protected val api: Single<T> get() = accountService.currentAccountSession
            .observeOn(Schedulers.io())
            .firstElement()
            .filter { it.get() != null }
            .map { it.get() }
            .toSingle()
            .map { dynamicLazyApiP7[it.domain] }

}