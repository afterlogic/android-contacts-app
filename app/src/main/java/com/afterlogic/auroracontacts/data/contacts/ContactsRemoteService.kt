package com.afterlogic.auroracontacts.data.contacts

import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

interface ContactsRemoteService {

    fun getContactsGroups(): Single<List<RemoteContactGroup>>

    fun getContacts(groupId: Long): Single<List<RemoteContact>>

    fun getFullContact(contactId: Long) : Single<RemoteFullContact>

    fun createContact(contact: RemoteFullContact) : Completable

    fun updateContact(contact: RemoteFullContact) : Completable

    fun deleteContact(id: Long) : Completable

}