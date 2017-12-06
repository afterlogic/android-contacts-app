package com.afterlogic.auroracontacts.presentation.background.authenticator

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by sashka on 05.04.16.
 * mail: sunnyday.development@gmail.com
 */
class AuthenticatorService : Service() {

    private val authenticator by lazy { Authenticator(this) }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator.iBinder
    }

}
