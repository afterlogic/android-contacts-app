package com.afterlogic.auroracontacts.data.api.p7

/**
 * Created by sashka on 10.10.16.
 *
 *
 * mail: sunnyday.development@gmail.com
 */

interface ApiP7 {

    companion object {

        const val AJAX = "?/Ajax/"

        fun completeUrl(base: String): String {
            return base + ApiP7.AJAX
        }

    }

    object Actions {
        const val SYSTEM_GET_APP_DATA = "SystemGetAppData"
        const val SYSTEM_LOGIN = "SystemLogin"
    }

    object Fields {
        const val ACTION = "Action"
        const val ACCOUNT_ID = "AccountID"
        const val TOKEN = "Token"
        const val AUTH_TOKEN = "AuthToken"
        const val EMAIL = "Email"
        const val INC_PASSWORD = "IncPassword"
    }

}