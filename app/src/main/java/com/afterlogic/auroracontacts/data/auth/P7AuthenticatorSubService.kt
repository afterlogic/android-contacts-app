package com.afterlogic.auroracontacts.data.auth

import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.error.ApiReturnedError
import com.afterlogic.auroracontacts.data.api.p7.model.SystemAppDataP7
import com.afterlogic.auroracontacts.data.auth.model.AuthorizedAuroraSession
import com.afterlogic.auroracontacts.data.errors.IllegalApiDataException
import com.google.gson.JsonSyntaxException
import io.reactivex.Single
import okhttp3.HttpUrl
import javax.inject.Inject

/**
 * Created by aleksandrcikin on 25.08.17.
 * mail: mail@sunnydaydev.me
 */

internal class P7AuthenticatorSubService @Inject
constructor(private val service: P7AuthenticatorNetworkService) : AuthenticatorSubService {

    override fun login(host: HttpUrl, email: String, pass: String): Single<AuthorizedAuroraSession> {

        return service.login(host, email, pass)
                .flatMap { (token, _) -> service.getLoggedSystemAppData(host, token)
                        .map { LoginResult(token, it) }
                }
                .map { handleLoginResult(it, host, email, pass) }

    }

    override fun isApiHost(host: HttpUrl): Single<Boolean> {

        return service.getSystemAppData(host)
                .map { true }
                .onErrorResumeNext {
                    if (isIncorrectApiVersionError(it)) Single.just(false)
                    else Single.error(it)
                }

    }

    private fun handleLoginResult(loginResult: LoginResult,
                                  host: HttpUrl,
                                  email: String?,
                                  pass: String?): AuthorizedAuroraSession {

        val systemAppData = loginResult.systemAppData
                ?: throw IllegalApiDataException("SystemAppData is null.")

        if (!systemAppData.isAuthorized) {
            throw IllegalApiDataException("Must be authorized authorized.")
        }

        val defaultAccountId = systemAppData.default

        val accounts = systemAppData.accounts
                ?: throw IllegalApiDataException("Accounts is null.")

        val defaultAccount = accounts.first { it.accountID == defaultAccountId }

        return AuthorizedAuroraSession(
                defaultAccount.email!!,
                systemAppData.token!!,
                loginResult.token,
                loginResult.accountId, // TODO: Check is really need it
                email!!,
                pass!!,
                host,
                ApiType.P7.code
        )

    }

    private inner class LoginResult(val token: String,
                                    systemAppDataPair: Pair<Long, SystemAppDataP7>) {

        val accountId: Long = systemAppDataPair.first
        val systemAppData: SystemAppDataP7? = systemAppDataPair.second

    }

    private fun isIncorrectApiVersionError(error: Throwable): Boolean {
        return error is JsonSyntaxException || error is ApiReturnedError
    }

}
