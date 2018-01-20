package com.afterlogic.auroracontacts.data.p7.auth

import com.afterlogic.auroracontacts.data.IllegalApiDataException
import com.afterlogic.auroracontacts.data.api.ApiReturnedError
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.p7.model.SystemAppDataP7
import com.afterlogic.auroracontacts.data.auth.AuthenticatorSubService
import com.afterlogic.auroracontacts.data.auth.model.AuthorizedAuroraSession
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
                .flatMap { (accountId, token) ->
                    service.getSystemAppData(host, token.token)
                            .map { LoginResult(accountId, token.token, it) }
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
                                  email: String,
                                  pass: String): AuthorizedAuroraSession {

        return loginResult.systemAppData
                .let(this::checkAuthorized)
                .let(this::extractAccountUserAndToken)
                .let { (user, appToken) ->

                    AuthorizedAuroraSession(
                            user,
                            appToken,
                            loginResult.token,
                            loginResult.accountId,
                            email,
                            pass,
                            host,
                            ApiType.P7.code
                    )

                }

    }

    private fun checkAuthorized(data: SystemAppDataP7): SystemAppDataP7 {
        return data.also {
            if (!it.isAuthorized) {
                throw IllegalApiDataException("Must be authorized authorized.")
            }
        }
    }

    private fun extractAccountUserAndToken(data: SystemAppDataP7): Pair<String, String> {

        return (data.accounts ?: throw IllegalApiDataException("Accounts is null."))
                .first { it.accountID == data.default }
                .email!! to data.token!!

    }

    private fun isIncorrectApiVersionError(error: Throwable): Boolean {
        return error is JsonSyntaxException || error is ApiReturnedError
    }

    private data class LoginResult(
            val accountId: Long,
            val token: String,
            val systemAppData: SystemAppDataP7
    )

}
