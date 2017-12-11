package com.afterlogic.auroracontacts.data.api.p7.util

import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.data.account.AuroraSession
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedException
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */



annotation class Auth(
        val value: String
)

object AuthValue {

    const val ACCOUNT_ID = "AuthValue.ACCOUNT_ID"
    const val APP_TOKEN = "AuthValue.APP_TOKEN"
    const val AUTH_TOKEN = "AuthValue.AUTO_TOKEN"

    const val STRING = "AuthValue.STRING"
    const val LONG = Long.MIN_VALUE

}

@AppScope
class AuthConverterFactoryP7 @Inject constructor() : Converter.Factory() {

    var currentSession: AuroraSession? = null

    override fun stringConverter(type: Type,
                                 annotations: Array<out Annotation>,
                                 retrofit: Retrofit): Converter<*, String>? {

        val annotation = annotations.find { it is Auth } as Auth? ?: return null

        return Converter<Any, String> converter@ {

            val session = currentSession ?: throw UserNotAuthorizedException()

            when(annotation.value) {

                AuthValue.APP_TOKEN -> if (it == AuthValue.STRING) session.appToken else it.toString()

                AuthValue.AUTH_TOKEN -> if (it == AuthValue.STRING) session.authToken else it.toString()

                AuthValue.ACCOUNT_ID -> if (it == AuthValue.LONG) session.accountId.toString() else it.toString()

                else -> it.toString()

            }

        }

    }

}