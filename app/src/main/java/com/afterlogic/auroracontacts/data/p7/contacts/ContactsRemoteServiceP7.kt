package com.afterlogic.auroracontacts.data.p7.contacts

import com.afterlogic.auroracontacts.data.auth.AuthResolver
import com.afterlogic.auroracontacts.data.contacts.ContactsRemoteService
import com.afterlogic.auroracontacts.data.contacts.RemoteContactGroup
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */
class ContactsRemoteServiceP7 @Inject constructor(
        private val authResolver: AuthResolver,
        private val cloudService: ContactsCloudServiceP7
) : ContactsRemoteService {

    override fun getContactsGroups(): Single<List<RemoteContactGroup>> = cloudService.getContactsGroups()
            .retryWhen(authResolver.checkAndResolveAuth)
            .map { it.map { RemoteContactGroup(it.name, it.id) } }

}