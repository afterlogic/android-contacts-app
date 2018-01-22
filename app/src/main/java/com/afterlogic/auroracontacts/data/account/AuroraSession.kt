package com.afterlogic.auroracontacts.data.account

import okhttp3.HttpUrl

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
data class AuroraSession(
    val user: String, val appToken: String, val authToken: String, val accountId: Long,
    val email: String?, val password: String?, val domain: HttpUrl, val apiVersion: Int
)