package com.afterlogic.auroracontacts.presentation.background.authenticator

import android.content.Intent
import android.os.IBinder
import com.afterlogic.auroracontacts.presentation.common.base.InjectionDaggerService

/**
 * Created by sashka on 05.04.16.
 * mail: sunnyday.development@gmail.com
 */
class AuthenticatorService : InjectionDaggerService<Authenticator>() {

    private val authenticator by inject { it }

    override fun onBind(intent: Intent): IBinder? = authenticator.iBinder

}
