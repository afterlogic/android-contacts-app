package com.afterlogic.auroracontacts.data.contacts

import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.data.db.ContactsDao
import com.afterlogic.auroracontacts.data.preferences.Prefs
import com.afterlogic.auroracontacts.data.util.RemoteServiceProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class ContactsRepository @Inject constructor(
        private val prefs: Prefs,
        private val dao: ContactsDao,
        private val remoteServiceProvider: RemoteServiceProvider<ContactsRemoteService>,
        private val mapper: ContactsMapper
) {

    private val remoteService get() = remoteServiceProvider.get()

    fun getContactsGroupsInfo(): Flowable<List<ContactGroupInfo>> {

        return dao.all.map { it.map(mapper::toPlain) }
                .filter { prefs.contactsFetched }
                .mergeWith(loadRemote().toFlowable())

    }

    fun setSyncEnabled(contactsGroup: ContactGroupInfo, enabled: Boolean) : Completable =
            Completable.fromAction { dao.setSyncEnabled(contactsGroup.id, enabled) }

    private fun loadRemote(): Completable {

        return remoteService.flatMap { it.getContactsGroups() }
                .map { it.map { mapper.toDbe(it) } }
                .doOnSuccess {
                    dao += it
                    prefs.contactsFetched = true
                }
                .toCompletable()

    }

}