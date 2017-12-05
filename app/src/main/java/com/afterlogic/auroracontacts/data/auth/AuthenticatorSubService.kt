package com.afterlogic.auroracontacts.data.auth


import com.afterlogic.auroracontacts.data.auth.model.AuthorizedAuroraSession
import io.reactivex.Single

/**
 * Created by aleksandrcikin on 25.08.17.
 * mail: mail@sunnydaydev.me
 */

internal interface AuthenticatorSubService {

    fun login(host: String, email: String, pass: String): Single<AuthorizedAuroraSession>

    fun isApiHost(host: String): Single<Boolean>

}
