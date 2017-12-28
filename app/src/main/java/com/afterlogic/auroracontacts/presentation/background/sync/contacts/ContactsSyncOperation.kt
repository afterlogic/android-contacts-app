package com.afterlogic.auroracontacts.presentation.background.sync.contacts

import android.accounts.Account
import android.content.ContentProviderClient
import android.provider.ContactsContract
import com.afterlogic.auroracontacts.data.contacts.ContactGroupInfo
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import com.afterlogic.auroracontacts.presentation.background.sync.BaseSyncOperation
import com.afterlogic.auroracontacts.presentation.background.sync.CustomContact
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsSyncOperation private constructor(
        account: Account,
        contentClient: ContentProviderClient,
        private val repository: ContactsRepository
) : BaseSyncOperation(account, contentClient) {

    class Factory @Inject constructor(private val repository: ContactsRepository) {

        fun create(account: Account, client: ContentProviderClient) : ContactsSyncOperation {
            return ContactsSyncOperation(account, client, repository)
        }

    }

    private val contacts = getContentClientHelper(ContactsContract.Contacts.CONTENT_URI)

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
                    val idsSqlIn = remoteContactIds.map { it.toString() } .toSqlIn()

                    contacts.delete("""
                        ${CustomContact.Contacts.SYNCED} = 1 AND
                        ${CustomContact.Contacts.REMOTE_ID} NOT IN $idsSqlIn
                    """.trimIndent())

                }

    }

    private fun syncContacts(contacts: List<RemoteContact>) : Completable {

        return contacts
                .filter {
                    // TODO: Check not changed
                    true
                }
                .map { sycContractFromRemote(it) }
                .let { Completable.concat(it) }

    }

    private fun sycContractFromRemote(contact: RemoteContact) : Completable = TODO()

    private fun getFirstLocal(): Single<List<ContactGroupInfo>> =
            repository.getContactsGroupsInfo(false)
                    .timeout(5, TimeUnit.SECONDS)
                    .firstOrError()

    private fun List<ContactGroupInfo>.isAllSyncEnabled(): Boolean =
            find { it.id == ContactsRepository.GROUP_ALL }?.syncing == true

}