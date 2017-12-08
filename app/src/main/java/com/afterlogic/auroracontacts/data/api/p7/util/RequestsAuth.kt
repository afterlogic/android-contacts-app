package com.afterlogic.auroracontacts.data.api.p7.util

import com.afterlogic.auroracontacts.core.rx.DisposableBag
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.account.AuroraSession
import com.afterlogic.auroracontacts.data.api.error.UserNotAuthorizedException
import com.afterlogic.auroracontacts.presentation.common.base.Subscribable
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */



annotation class Auth

object AuthValue {

    val ACCOUNT_ID = Long.MIN_VALUE

    val APP_TOKEN = "AuthValue.APP_TOKEN"
    val AUTH_TOKEN = "AuthValue.AUTO_TOKEN"

}

class AuthConverterFactoryP7 @Inject constructor(
        accountService: AccountService,
        subscriber: Subscriber
) : Converter.Factory(), Subscribable by Subscribable.Default(subscriber, DisposableBag()) {

    private var currentSession: AuroraSession? = null

    init {

        accountService.currentAccountSession
                .retry()
                .defaultSchedulers()
                .subscribeIt { currentSession = it.get() }

    }

    override fun stringConverter(type: Type?, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, String>? {

        if (annotations.find { it is Auth } == null) return null

        return Converter<Any, String> {

            val session = currentSession ?: throw UserNotAuthorizedException()

            when(it) {

                AuthValue.APP_TOKEN -> session.appToken

                AuthValue.AUTH_TOKEN -> session.authToken

                AuthValue.ACCOUNT_ID -> session.accountId.toString()

                else -> it.toString()

            }

        }

    }

}