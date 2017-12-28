package com.afterlogic.auroracontacts.presentation.background.sync.contacts

import android.accounts.Account
import android.content.ContentProviderClient
import com.afterlogic.auroracontacts.data.contacts.ContactGroupInfo
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsSyncOperation private constructor(
        private val account: Account,
        private val contentClient: ContentProviderClient,
        private val repository: ContactsRepository
) {

    class Factory @Inject constructor(private val repository: ContactsRepository) {

        fun create(account: Account, client: ContentProviderClient) : ContactsSyncOperation {
            return ContactsSyncOperation(account, client, repository)
        }

    }

    fun sync() : Completable = repository.loadRemote()
            .flatMap { getFirstLocal() }
            .flatMapCompletable {

                // Check all
                val groupIds = if (it.isAllSyncEnabled()) {
                    listOf(ContactsRepository.GROUP_ALL)
                } else {
                    it.filter { it.syncing } .map { it.id }
                }

                syncGroups(groupIds)

            }
            .doOnError(Timber::d)

    private fun syncGroups(groupIds: List<Long>): Completable {

        val remoteContactIds = mutableListOf<Long>()

        return groupIds
                .map {
                    repository.getRemoteContacts(it)
                            .doOnSuccess { it.map { it.id } .let { remoteContactIds.addAll(it) } }
                            .flatMapCompletable { syncContacts(it) }
                }
                .let { Completable.concat(it) }
                .doFinally {
                    // Remote not exists in remote contacts
                }

    }

    private fun syncContacts(contacts: List<RemoteContact>) : Completable {
        return Completable.complete()
    }

    private fun getFirstLocal(): Single<List<ContactGroupInfo>> =
            repository.getContactsGroupsInfo(false)
                    .timeout(5, TimeUnit.SECONDS)
                    .firstOrError()

    private fun List<ContactGroupInfo>.isAllSyncEnabled(): Boolean =
            find { it.id == ContactsRepository.GROUP_ALL }?.syncing == true

}