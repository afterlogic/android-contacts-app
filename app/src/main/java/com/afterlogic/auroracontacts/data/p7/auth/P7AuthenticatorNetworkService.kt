package com.afterlogic.auroracontacts.data.p7.auth

import com.afterlogic.auroracontacts.data.api.p7.model.AuthTokenP7
import com.afterlogic.auroracontacts.data.api.p7.model.SystemAppDataP7
import com.afterlogic.auroracontacts.data.api.p7.util.checkResponseAndGetData
import com.afterlogic.auroracontacts.data.p7.api.AuthApiP7
import com.afterlogic.auroracontacts.data.p7.api.DynamicLazyApiP7
import io.reactivex.Single
import okhttp3.HttpUrl
import javax.inject.Inject

/**
 * Created by aleksandrcikin on 25.08.17.
 * mail: mail@sunnydaydev.me
 */

internal class P7AuthenticatorNetworkService @Inject
constructor(private val api: DynamicLazyApiP7<AuthApiP7>) {

    fun getSystemAppData(host: HttpUrl, authToken: String? = null): Single<SystemAppDataP7> {

        return api[host].getSystemAppData(authToken)
                .checkResponseAndGetData()

    }

    fun login(host: HttpUrl, login: String, pass: String): Single<Pair<Long, AuthTokenP7>> {

        return api[host].login(login, pass)
                .checkResponseAndGetData { it.accountId!! to it.data!! }

    }

}
