package com.afterlogic.auroracontacts.data.account

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.TargetApi
import android.os.Build
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.core.rx.toMaybe
import com.afterlogic.auroracontacts.core.util.Optional
import com.afterlogic.auroracontacts.core.util.toOptional
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.p7.ApiP7
import com.afterlogic.auroracontacts.data.auth.model.AuthorizedAuroraSession
import com.afterlogic.auroracontacts.presentation.AppScope
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.HttpUrl
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class AccountService @Inject constructor(context: App) {

    companion object {

        val ACCOUNT_TYPE = "com.afterlogic.aurora"
        val FILE_SYNC_AUTHORITY = "com.afterlogic.aurora.filesync.provider"

        val ACCOUNT_ID = "account_id"
        val APP_TOKEN = "app_token"
        val AUTH_TOKEN = "auth_token"
        val DOMAIN = "domain"
        val API_VERSION = "apiVersion"
        val HAS_SESSION = "hasSession"
        val EMAIL = "email"

    }

    private val accountManager = AccountManager.get(context)

    private val currentAccount = Observable.defer { CurrentAccountSource(accountManager) }.cache()

    val currentAccountSession: Observable<Optional<AuroraSession>>
        get() = currentAccount
                .map { it.get().toOptional { it.toSession() } }
                .distinctUntilChanged { f, s -> f.get() == s.get() }

    fun updateCurrentAccount(authData: AuthorizedAuroraSession): Completable {

        return currentAccount
                .firstOrError()
                .doOnSuccess {

                    val account = it.get() ?: throw AccountNotExistError()

                    if (account.name != authData.user) {
                        throw AnotherAcountExistError(account.name)
                    }

                    accountManager.setUserData(account, HAS_SESSION, "true")

                    accountManager.setUserData(account, ACCOUNT_ID, authData.accountId.toString())
                    accountManager.setUserData(account, DOMAIN, authData.domain.toString())
                    accountManager.setUserData(account, APP_TOKEN, authData.appToken)
                    accountManager.setUserData(account, ApiP7.Fields.AUTH_TOKEN, authData.authToken)
                    accountManager.setUserData(account, API_VERSION, authData.apiVersion.toString())
                    accountManager.setUserData(account, EMAIL, authData.email)
                    accountManager.setPassword(account, authData.password)

                }
                .toCompletable()

    }

    private fun createOrUpdateAccount(authData: AuthorizedAuroraSession): Completable {

        return Completable
                .fromAction { createAccountIfNotExist(authData) }
                .andThen(updateCurrentAccount(authData))

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun removeCurrentAccount(): Completable {

        return currentAccount
                .firstOrError()
                .flatMapMaybe { it.get().toMaybe() }
                .doOnSuccess {

                    val removed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                        accountManager.removeAccountExplicitly(it)

                    } else {

                        @Suppress("DEPRECATION")
                        accountManager.removeAccount(it, null, null).result

                    }

                    if (!removed) {
                        throw AccountActionError(AccountActionError.Action.REMOVING)
                    }

                }
                .ignoreElement()

    }

    private fun createAccountIfNotExist(authData: AuthorizedAuroraSession): Completable {

        return currentAccount
                .firstOrError()
                .filter { it.get() == null }
                .doOnSuccess { createAccount(authData) }
                .ignoreElement()

    }

    private fun createAccount(authData: AuthorizedAuroraSession) {

        val account = Account(authData.user, ACCOUNT_TYPE)

        if(!accountManager.addAccountExplicitly(account, null , null)) {
            throw AccountActionError(AccountActionError.Action.ADDING)
        }

    }
    
    private fun Account.toSession(): AuroraSession {

        val am = accountManager

        return AuroraSession(
                name,
                am.getUserData(this, AccountService.APP_TOKEN),
                am.getUserData(this, AccountService.AUTH_TOKEN),
                am.getUserData(this, AccountService.ACCOUNT_ID)
                        .toLongOrNull() ?: -1,
                am.getUserData(this, AccountService.EMAIL),
                am.getPassword(this),
                HttpUrl.parse(am.getUserData(this, AccountService.DOMAIN))!!,
                am.getUserData(this, AccountService.API_VERSION)
                        .toIntOrNull() ?: ApiType.UNKNOWN.code
        )

    }

}