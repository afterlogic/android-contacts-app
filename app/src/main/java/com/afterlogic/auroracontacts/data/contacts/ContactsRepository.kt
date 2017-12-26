package com.afterlogic.auroracontacts.data.contacts

import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.data.util.RemoteServiceProvider
import javax.inject.Inject

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class ContactsRepository @Inject constructor(
        private val remoteServiceProvider: RemoteServiceProvider<ContactsRemoteService>
) {

    private val remoteService get() = remoteServiceProvider.get()

}