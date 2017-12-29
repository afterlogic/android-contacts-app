package com.afterlogic.auroracontacts.presentation.background.sync.contacts

import android.accounts.Account
import android.content.ContentProviderClient
import android.provider.ContactsContract
import com.afterlogic.auroracontacts.data.api.NullApiResultError
import com.afterlogic.auroracontacts.data.contacts.ContactGroupInfo
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import com.afterlogic.auroracontacts.data.contacts.RemoteFullContact
import com.afterlogic.auroracontacts.data.preferences.Prefs
import com.afterlogic.auroracontacts.presentation.background.sync.BaseSyncOperation
import com.afterlogic.auroracontacts.presentation.background.sync.CustomContact
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private typealias Data = ContactsContract.Data
private typealias Contacts = ContactsContract.Contacts
private typealias RawContact = ContactsContract.RawContacts

class ContactsSyncOperation private constructor(
        private val prefs: Prefs,
        account: Account,
        contentClient: ContentProviderClient,
        private val repository: ContactsRepository
) : BaseSyncOperation(account, contentClient) {

    class Factory @Inject constructor(
            private val prefs: Prefs,
            private val repository: ContactsRepository
    ) {

        fun create(account: Account, client: ContentProviderClient) : ContactsSyncOperation {
            return ContactsSyncOperation(prefs, account, client, repository)
        }

    }

    private val rawContactsClient = getContentClientHelper(ContactsContract.RawContacts.CONTENT_URI)

    fun sync() : Completable = repository.loadRemote()
            .flatMap { getFirstLocal() }
            .flatMapCompletable {

                // Check all
                val groupIds = if (it.isAllSyncEnabled()) {
                    listOf(ContactsRepository.GROUP_ALL)
                } else {
                    it.filter { it.syncing } .map { it.id }
                }

                val cycleId = ++prefs.lastContactSyncCycleId

                syncGroups(groupIds, cycleId)

            }
            .doOnError(Timber::d)

    private fun syncGroups(groupIds: List<Long>, syncCycleId: Int): Completable {

        return syncGroupsRecursively(groupIds, emptySet(), syncCycleId)
                .doOnSuccess {

                    if (it.isEmpty()) return@doOnSuccess

                    // Remote not exists in remote rawContactsClient
                    val idsSqlIn = it.map { it.toString() } .toSqlIn()

                    rawContactsClient.delete("""
                        ${CustomContact.RawContacts.SYNCED} = 1 AND
                        ${CustomContact.RawContacts.REMOTE_ID} NOT IN $idsSqlIn
                    """.trimIndent())

                }
                .toCompletable()

    }

    /**
     * Return synced contacts ids.
     */
    private fun syncGroupsRecursively(
            groups: List<Long>,
            contacts: Set<Long>,
            syncCycleId: Int
    ): Single<Set<Long>> {

        if (groups.isEmpty()) return Single.just(contacts)

        return syncContactsGroup(groups.first(), syncCycleId) { !contacts.contains(it) }
                .flatMap {
                    val leftGroups = groups.drop(1)
                    val idsSum = contacts + it
                    syncGroupsRecursively(leftGroups, idsSum, syncCycleId)
                }

    }

    /**
     * Return synced contacts ids.
     */
    private fun syncContactsGroup(id: Long, syncCycleId: Int,
                                  synced: (Long) -> Boolean) : Single<List<Long>> {

        return repository.getRemoteContacts(id)
                .flatMap {

                    // TODO: Check not changed (currently ETag not working, always empty).
                    // Now filter only not already synced in one sync cycle.
                    it.filter { synced(it.id) }
                            .map { syncContactFromRemote(it, syncCycleId) }
                            .let { Completable.concat(it) }
                            .andThen(Single.fromCallable { it.map { it.id } })

                }

    }

    private fun syncContactFromRemote(contact: RemoteContact, syncCycleId: Int) : Completable {

        return repository.getFullContact(contact.id)
                .doOnSuccess { storeRemoteContact(it, syncCycleId) }
                .toCompletable()
                .onErrorResumeNext {
                    when(it) {
                        is NullApiResultError -> Completable.complete()
                        else -> Completable.error(it)
                    }
                }

    }

    private fun storeRemoteContact(contact: RemoteFullContact, syncCycleId: Int) {

        val cursor = rawContactsClient.query(
                projection = arrayOf(
                        RawContact.CONTACT_ID,
                        CustomContact.RawContacts.SYNC_CYCLE_ID
                ),
                selection = "${CustomContact.RawContacts.REMOTE_ID} = ${contact.id}"
        ) ?: throw UnexpectedNullCursorException()

        val exists = cursor.count == 1

        val synced = cursor.moveToFirst() &&
                cursor.getInt(CustomContact.RawContacts.SYNC_CYCLE_ID) == syncCycleId

        cursor.close()

        if (exists && synced) return



    }

    private fun getFirstLocal(): Single<List<ContactGroupInfo>> =
            repository.getContactsGroupsInfo(false)
                    .timeout(5, TimeUnit.SECONDS)
                    .firstOrError()

    private fun List<ContactGroupInfo>.isAllSyncEnabled(): Boolean =
            find { it.id == ContactsRepository.GROUP_ALL }?.syncing == true

}