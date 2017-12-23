package com.afterlogic.auroracontacts.data.api.p7.util

import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.data.account.AuroraSession
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedException
import okhttp3.MediaType
import okhttp3.RequestBody
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

    override fun requestBodyConverter(type: Type, parameterAnnotations: Array<out Annotation>, methodAnnotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, RequestBody>? {

        val annotation = parameterAnnotations.find { it is Auth } as Auth? ?: return null

        return Converter<Any, RequestBody> {
            val value = getAuthValue(annotation, it)
            RequestBody.create(MediaType.parse("text/plain"), value)
        }

    }

    override fun stringConverter(type: Type,
                                 annotations: Array<out Annotation>,
                                 retrofit: Retrofit): Converter<*, String>? {

        val annotation = annotations.find { it is Auth } as Auth? ?: return null

        return Converter<Any, String> {
            getAuthValue(annotation, it)
        }

    }

    private fun getAuthValue(annotation: Auth, value: Any): String {

        val session = currentSession ?: throw UserNotAuthorizedException()

        return when(annotation.value) {

            AuthValue.APP_TOKEN -> if (value == AuthValue.STRING) session.appToken else value.toString()

            AuthValue.AUTH_TOKEN -> if (value == AuthValue.STRING) session.authToken else value.toString()

            AuthValue.ACCOUNT_ID -> if (value == AuthValue.LONG) session.accountId.toString() else value.toString()

            else -> value.toString()

        }

    }

}