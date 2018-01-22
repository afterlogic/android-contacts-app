package com.afterlogic.auroracontacts.presentation.foreground.unsuportedApi

import com.afterlogic.auroracontacts.data.account.AccountService
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
class UnsupportedApiInteractor @Inject constructor(
        private val accountService: AccountService
) {

    val currentAccountName = accountService.account.map { it.get()?.name ?: "" }

    fun logout(): Completable = accountService.removeCurrentAccount()

}