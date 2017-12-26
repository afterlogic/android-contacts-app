package com.afterlogic.auroracontacts.data.contacts

import io.reactivex.Single

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

interface ContactsRemoteService {
    fun getContactsGroups(): Single<List<RemoteContactGroup>>
}