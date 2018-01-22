package com.afterlogic.auroracontacts.presentation.foreground.login

import com.afterlogic.auroracontacts.data.UnsupportedApiError
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.auth.AuthenticatorService
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.HttpUrl
import javax.inject.Inject

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
class LoginInteractor @Inject constructor(
        private val authenticatorService: AuthenticatorService,
        private val accountService: AccountService
) {

    fun checkHost(host: String): Single<HttpUrl> {

        return Single.defer {

            host.let(this::checkAndParseUrl)
                    .let(this::collectUrlsForCheck)
                    .map(this::checkApiType)
                    .let { Maybe.concat(it) }
                    .firstElement()
                    .switchIfEmpty(Maybe.error(UnsupportedApiError()))
                    .toSingle()

        }

    }

    private fun checkAndParseUrl(host: String): Pair<HttpUrl, Boolean> {

        return if (host.contains("://")) {
            HttpUrl.parse(host) to true
        }  else {
            HttpUrl.parse("http://" + host) to false
        } .let { (url, manual) ->

            url ?: throw IllegalArgumentException("Unparselable url: $host")

            url to manual

        }

    }

    private fun collectUrlsForCheck(checkedUrl: Pair<HttpUrl, Boolean>): List<HttpUrl> {

        return checkedUrl
                .let { (url, manual) ->
                    if (manual) {
                        listOf(url)
                    } else {
                        listOf(
                                url.newBuilder().scheme("https").build(),
                                url.newBuilder().scheme("http").build()
                        )
                    }
                }

    }

    private fun checkApiType(url: HttpUrl): Maybe<HttpUrl> {

        return authenticatorService.getApiType(url)
                .filter { ApiType.supportedApiTypes.contains(it) }
                .map { url }

    }

    fun login(host: HttpUrl, email: String, password: String): Completable {

        return authenticatorService.login(host, email, password)
                .flatMapCompletable(accountService::createOrUpdateAccount)

    }

}