package com.afterlogic.auroracontacts.data.account

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.TargetApi
import android.os.Build
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.core.rx.toMaybe
import com.afterlogic.auroracontacts.core.util.Optional
import com.afterlogic.auroracontacts.data.auth.model.AuthorizedAuroraSession
import com.afterlogic.auroracontacts.presentation.AppScope
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.HttpUrl
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class AccountService @Inject constructor(context: App) {

    companion object {

        val ACCOUNT_TYPE = "com.afterlogic.aurora"

        val ACCOUNT_ID = "account_id"
        val APP_TOKEN = "app_token"
        val AUTH_TOKEN = "auth_token"
        val DOMAIN = "domain"
        val API_VERSION = "apiVersion"
        val HAS_SESSION = "hasSession"
        val EMAIL = "email"

    }

    private val accountManager = AccountManager.get(context)

    private val account = Observable.defer { CurrentAccountSource(accountManager) }

    val accountSession: Observable<Optional<AuroraSession>> get() {

        return account
                .map<Optional<AuroraSession>> {

                    val account = it.get() ?: return@map Optional()

                    Timber.d("Account: $account")

                    if (!account.userData.hasSessionData) {
                        return@map Optional()
                    }

                    Optional(account.toSession())

                }
                .distinctUntilChanged { f, s -> f.get() == s.get() }

    }

    fun updateCurrentAccount(authData: AuthorizedAuroraSession): Completable {

        return account
                .firstOrError()
                .doOnSuccess {

                    val account = it.get() ?: throw AccountNotExistError()

                    if (account.name != authData.user) {
                        throw AnotherAcountExistError(account.name)
                    }
                    
                    val userData = account.userData
                    
                    userData.hasSessionData = true

                    userData[ACCOUNT_ID] = authData.accountId.toString()
                    userData[DOMAIN] = authData.domain.toString()
                    userData[APP_TOKEN] = authData.appToken
                    userData[AUTH_TOKEN] = authData.authToken
                    userData[API_VERSION] = authData.apiVersion.toString()
                    userData[EMAIL] = authData.email
                    
                    account.password = authData.password

                }
                .toCompletable()

    }

    fun createOrUpdateAccount(authData: AuthorizedAuroraSession): Completable {

        return createAccountIfNotExist(authData)
                .andThen(updateCurrentAccount(authData))

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun removeCurrentAccount(): Completable {

        return account
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

        return account
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

        val userData = userData

        fun error(message: String? = null): Nothing {
            throw AuroraAccountSessionParseError(message)
        }

        if (!userData.hasSessionData) {
            error("Session has no data")
        }

        return AuroraSession(
                name,
                userData[APP_TOKEN] ?: error(),
                userData[AUTH_TOKEN] ?: error(),
                userData[ACCOUNT_ID]?.toLongOrNull() ?: error(),
                userData[EMAIL] ?: error(),
                password ?: error(),
                userData[DOMAIN]?.let { HttpUrl.parse(it) } ?: error(),
                userData[API_VERSION]?.toIntOrNull() ?: error()
        )

    }
    
    private var Account.password: String?
        get() = accountManager.getPassword(this)
        set(value) { accountManager.setPassword(this, value) }
    
    private val Account.userData: AccountService.AccountUserData get() = AccountUserData(this)

    private var AccountUserData.hasSessionData: Boolean
        get() = this[HAS_SESSION]?.toBoolean() == true
        set(value) { this[HAS_SESSION] = if (value) "true" else "false" }
    
    private inner class AccountUserData(private val account: Account) {

        operator fun get(key: String): String? {
            return accountManager.getUserData(account, key)
        }

        operator fun set(key: String, value: String?) {
            accountManager.setUserData(account, key, value)
        }
        
    }

}