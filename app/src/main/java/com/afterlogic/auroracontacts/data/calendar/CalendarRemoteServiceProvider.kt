package com.afterlogic.auroracontacts.data.calendar

import com.afterlogic.auroracontacts.data.NotSupportedApiError
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedException
import com.afterlogic.auroracontacts.data.p7.calendars.CalendarsRemoteServiceP7
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 11.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CalendarRemoteServiceProvider @Inject constructor(
        private val accountService: AccountService,
        private val p7RemoteService: CalendarsRemoteServiceP7
) {

    fun get(): Single<CalendarRemoteService> = accountService.accountSession
            .firstOrError()
            .map {

                val session = it.get() ?: throw UserNotAuthorizedException()
                val apiType = ApiType.byCode(session.apiVersion) ?:
                        throw NotSupportedApiError()

                when(apiType) {
                    ApiType.P7 -> p7RemoteService
                    else -> throw NotSupportedApiError()
                }

            }

}