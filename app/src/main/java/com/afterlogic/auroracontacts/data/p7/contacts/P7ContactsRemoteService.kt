package com.afterlogic.auroracontacts.data.p7.contacts

import com.afterlogic.auroracontacts.data.auth.AuthResolver
import com.afterlogic.auroracontacts.data.contacts.ContactsRemoteService
import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import com.afterlogic.auroracontacts.data.contacts.RemoteContactGroup
import com.afterlogic.auroracontacts.data.contacts.RemoteFullContact
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */
class P7ContactsRemoteService @Inject constructor(
        private val authResolver: AuthResolver,
        private val cloudService: P7ContactsCloudService,
        private val mapper: P7ContactsMapper
) : ContactsRemoteService {

    override fun getContactsGroups(): Single<List<RemoteContactGroup>> {

        return cloudService.getContactsGroups()
                .retryWhen(authResolver.checkAndResolveAuth)
                .map { it.map { RemoteContactGroup(it.name, it.id) } }

    }

    override fun getContacts(groupId: Long): Single<List<RemoteContact>> {

        return cloudService.getContacts(groupId, 0)
                .flatMap { collectAll(groupId, it) }
                .retryWhen(authResolver.checkAndResolveAuth)

    }

    override fun getFullContact(contactId: Long): Single<RemoteFullContact> {

        return cloudService.getFullContact(contactId)
                .retryWhen(authResolver.checkAndResolveAuth)
                .map(mapper::toPlain)

    }

    override fun createContact(contact: RemoteFullContact): Completable = Completable.defer {
        cloudService.createContact(mapper.toDto(contact))
                .retryWhen(authResolver.checkAndResolveAuth)
                //.doOnSuccess { if (!it) throw IllegalStateException("Api return false.") }
                .toCompletable()
    }

    override fun updateContact(contact: RemoteFullContact): Completable = Completable.defer {
        cloudService.updateContact(mapper.toDto(contact))
                .retryWhen(authResolver.checkAndResolveAuth)
                .doOnSuccess { if (!it) throw IllegalStateException("Api return false.") }
                .toCompletable()
    }

    private fun collectAll(groupId: Long, data: P7ContactsData): Single<List<RemoteContact>> {

        if (data.contactCount == data.list.size) return Single.just(data.list)

        val summary = mutableListOf<RemoteContact>()

        summary.addAll(data.list)

        return Single.defer { cloudService.getContacts(groupId, summary.size) }
                .retryWhen(authResolver.checkAndResolveAuth)
                .doOnSuccess { summary.addAll(it.list) }
                .repeatUntil { summary.size == data.contactCount }
                .ignoreElements()
                .andThen(Single.fromCallable { summary.toList() })

    }

}