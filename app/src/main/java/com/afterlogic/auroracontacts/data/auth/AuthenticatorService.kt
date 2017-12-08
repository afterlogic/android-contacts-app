package com.afterlogic.auroracontacts.data.auth

import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.auth.model.AuthorizedAuroraSession
import com.afterlogic.auroracontacts.data.NotSupportedApiError
import com.afterlogic.auroracontacts.data.p7.auth.P7AuthenticatorSubService
import com.afterlogic.auroracontacts.presentation.AppScope
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.HttpUrl
import javax.inject.Inject

/**
 * Created by aleksandrcikin on 25.08.17.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class AuthenticatorService @Inject
internal constructor(
        p7AuthenticatorService: P7AuthenticatorSubService
        //p8AuthenticatorService: P8AuthenticatorSubService
) {

    private val p7AuthenticatorSubService: AuthenticatorSubService
    //private val p8AuthenticatorSubService: AuthenticatorSubService

    init {

        this.p7AuthenticatorSubService = p7AuthenticatorService
        //this.p8AuthenticatorSubService = p8AuthenticatorService

    }

    fun login(host: HttpUrl, login: String, password: String): Single<AuthorizedAuroraSession> {
        return getAuthenticatorService(host)
                .flatMap { it.login(host, login, password) }
    }

    fun getApiType(host: HttpUrl): Single<ApiType> {

        // TODO add api 8
        return listOf(p7AuthenticatorSubService to ApiType.P7)
                .map { (service, apiType) -> service.isApiHost(host)
                        .flatMapMaybe {
                            if (it) Maybe.just(apiType)
                            else Maybe.empty()
                        }
                }
                .let { Maybe.concat(it) }
                .first(ApiType.UNKNOWN)

    }

    private fun getAuthenticatorService(host: HttpUrl): Single<AuthenticatorSubService> {

        return getApiType(host)
                .map {
                    if (it == ApiType.P7) p7AuthenticatorSubService
                    else throw NotSupportedApiError(it)
                }

    }


}
