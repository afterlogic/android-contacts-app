package com.afterlogic.auroracontacts.data.auth.model

import okhttp3.HttpUrl

/**
 * Aurora session.
 * Handle account id, app token, auth token.
 */

class AuthorizedAuroraSession(val user: String,
                              val appToken: String,
                              val authToken: String,
                              val accountId: Long,
                              val email: String,
                              val password: String,
                              val domain: HttpUrl,
                              val apiVersion: Int)
