package com.afterlogic.auroracontacts.presentation.foreground.login

import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.auth.AuthenticatorService
import com.afterlogic.auroracontacts.data.errors.NotSupportedApiError
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.HttpUrl
import java.util.*
import javax.inject.Inject

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
class LoginInteractor @Inject constructor(
        private val authenticatorService: AuthenticatorService
) {

    fun checkHost(host: String): Single<HttpUrl> {

        return Single.defer {

            val manual: Boolean

            val domain: HttpUrl?
            if (host.contains("://")) {
                manual = true
                domain = HttpUrl.parse(host)
            } else {
                manual = false
                domain = HttpUrl.parse("http://" + host)
            }

            if (domain == null) {
                throw IllegalArgumentException("Unparselable url: $host")
            }

            val domains = ArrayList<HttpUrl>()

            if (manual) {
                domains.add(domain)
            } else {
                domains.add(domain.newBuilder().scheme("https").build())
                domains.add(domain.newBuilder().scheme("http").build())
            }

            domains
                    .map {url -> authenticatorService.getApiType(url.toString())
                            .filter { it != ApiType.UNKNOWN }
                            .map { url }
                    }
                    .let { Maybe.merge(it) }
                    .firstElement()
                    .switchIfEmpty(Maybe.error(NotSupportedApiError()))
                    .toSingle()

        }

    }

}