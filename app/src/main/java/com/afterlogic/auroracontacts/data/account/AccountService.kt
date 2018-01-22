package com.afterlogic.auroracontacts.data.account

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.TargetApi
import android.os.Build
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.core.rx.toMaybe
import com.afterlogic.auroracontacts.core.util.Optional
import com.afterlogic.auroracontacts.data.auth.model.AuthorizedAuroraSession
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.HttpUrl
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class AccountService @Inject constructor(context: App) {

    companion object {

        const val ACCOUNT_TYPE = "com.afterlogic.aurora"

    }

    private val accountManager = AccountManager.get(context)

    val account = Observable.defer { CurrentAccountSource(accountManager) }

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

                    val userData = account.userData

                    val currentDomain = userData.domain

                    if (account.name != authData.user ||
                            currentDomain != null && currentDomain != authData.domain.toString()) {

                        throw AnotherAccountExistError(account.name, currentDomain ?: "")

                    }
                    
                    userData.hasSessionData = true
                    userData.accountId = authData.accountId
                    userData.domain = authData.domain.toString()
                    userData.appToken = authData.appToken
                    userData.authToken = authData.authToken
                    userData.apiVersion = authData.apiVersion
                    userData.email = authData.email
                    userData.credentialsVersion = AccountUserData.CREDENTIALS_VERSION

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
            error("Session has no data.")
        }

        return AuroraSession(
                name,
                userData.appToken ?: error(),
                userData.authToken ?: error(),
                userData.accountId ?: error(),
                userData.email ?: if (userData.credentialsVersion == 2) name else null,
                password,
                userData.domain?.let { HttpUrl.parse(it) } ?: error(),
                userData.apiVersion ?: error()
        )

    }
    
    private var Account.password: String?
        get() = accountManager.getPassword(this)
        set(value) { accountManager.setPassword(this, value) }
    
    private val Account.userData: AccountService.AccountUserData get() =
        AccountUserData(this, accountManager)
    
    private class AccountUserData(private val account: Account, private val accountManager: AccountManager) {

        companion object {

            private const val HAS_SESSION = "hasSession"
            private const val ACCOUNT_ID = "account_id"
            private const val APP_TOKEN = "app_token"
            private const val AUTH_TOKEN = "auth_token"
            private const val DOMAIN = "domain"
            private const val API_VERSION = "apiVersion"
            private const val EMAIL = "email"

            private const val KEY_CREDENTIALS_VERSION = "credentials_version"

            const val CREDENTIALS_VERSION = 4

        }

        var accountId by userData(ACCOUNT_ID) { it.toLongOrNull() }
        var email by userData(EMAIL) { it }
        var domain by userData(DOMAIN) { it }
        var apiVersion by userData(API_VERSION) { it.toIntOrNull() }
        var appToken by userData(APP_TOKEN) { it }
        var authToken by userData(AUTH_TOKEN) { it }
        var credentialsVersion by userData(KEY_CREDENTIALS_VERSION) { it.toIntOrNull() }

        private var hasSessionFlag by userData(HAS_SESSION) { it.toBoolean() }

        var hasSessionData: Boolean
            get() {
                return hasSessionFlag == true ||
                        appToken != null &&
                        authToken != null &&
                        accountId != null &&
                        domain != null
            }
            set(value) { hasSessionFlag = value }

        private inline fun <reified T> userData(
                name: String,
                crossinline set: (T) -> String? = { it?.toString() },
                crossinline get: (String) -> T?
        ): ReadWriteProperty<AccountUserData, T?> = object: ReadWriteProperty<AccountUserData, T?> {

            override fun setValue(thisRef: AccountUserData, property: KProperty<*>, value: T?) {
                accountManager.setUserData(account, name, value?.let(set))
            }

            override fun getValue(thisRef: AccountUserData, property: KProperty<*>): T? {
                return accountManager.getUserData(account, name)?.let(get)
            }

        }

    }

}