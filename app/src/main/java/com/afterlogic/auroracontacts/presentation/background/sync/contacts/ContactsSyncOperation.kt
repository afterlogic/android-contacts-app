package com.afterlogic.auroracontacts.presentation.background.sync.contacts

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentValues
import android.provider.ContactsContract
import com.afterlogic.auroracontacts.data.api.ApiNullResultError
import com.afterlogic.auroracontacts.data.contacts.ContactGroupInfo
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import com.afterlogic.auroracontacts.data.contacts.RemoteFullContact
import com.afterlogic.auroracontacts.presentation.background.sync.BaseSyncOperation
import com.afterlogic.auroracontacts.presentation.background.sync.CustomContact
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsSyncOperation private constructor(
        private val account: Account,
        contentClient: ContentProviderClient,
        private val repository: ContactsRepository
) : BaseSyncOperation(account, contentClient) {

    class Factory @Inject constructor(
            private val repository: ContactsRepository
    ) {

        fun create(account: Account, client: ContentProviderClient) : ContactsSyncOperation {
            return ContactsSyncOperation(account, client, repository)
        }

    }

    private val rawContactsClient = getContentClientHelper(ContactsContract.RawContacts.CONTENT_URI)
    private val dataClient = getContentClientHelper(ContactsContract.Data.CONTENT_URI)

    private val eTags = WeakHashMap<RemoteContact, String>()
    private val RemoteContact.eTag: String get() = eTags.getOrPut(this) {
        // TODO: replace reflection
        RemoteContact::class.java.methods
                .map { it.invoke(this) ?: "" }
                .joinToString(":")
                .also {
                    Timber.d("Hash source: $it")
                }
                .let { DigestUtil.toSha256(it) }
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

        return syncGroupsRecursively(groupIds)
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
            contacts: Set<Long> = emptySet()
    ): Single<Set<Long>> {

        if (groups.isEmpty()) return Single.just(contacts)

        return syncContactsGroup(groups.first()) { contacts.contains(it) }
                .flatMap {
                    val leftGroups = groups.drop(1)
                    val idsSum = contacts + it
                    syncGroupsRecursively(leftGroups, idsSum)
                }

    }

    /**
     * Return synced contacts ids.
     */
    private fun syncContactsGroup(id: Long, synced: (Long) -> Boolean) : Single<List<Long>> {

        return repository.getRemoteContacts(id)
                .flatMap {

                    it.filterNot { synced(it.id) || isNotChanged(it)}
                            .map { syncContactFromRemote(it) }
                            .let { Completable.concat(it) }
                            .andThen(Single.fromCallable { it.map { it.id } })

                }

    }

    private fun syncContactFromRemote(contact: RemoteContact) : Completable {

        return repository.getFullContact(contact.id)
                .doOnSuccess { storeRemoteContact(it, contact) }
                .toCompletable()
                .onErrorResumeNext {
                    when(it) {
                        is ApiNullResultError -> Completable.fromAction {
                            storeRemoteContact(null, contact)
                        }
                        else -> Completable.error(it)
                    }
                }

    }

    private fun storeRemoteContact(fullContact: RemoteFullContact?, contact: RemoteContact) {

        val localId = getLocalContactId(contact.id) ?: insertNewRawContact(contact.id)

        ContentValues().apply {
            put(ContactsContract.RawContacts.RAW_CONTACT_IS_READ_ONLY, contact.isReadOnly)
        } .also { rawContactsClient.update(it, "${ContactsContract.RawContacts.CONTACT_ID} = $localId") }

        // Delete all current data
        dataClient.delete("${CustomContact.RawContacts.REMOTE_ID} = ${contact.id}")

        insertData(localId, ContactsContract.CommonDataKinds.StructuredName.MIMETYPE) {

            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)

            if (fullContact == null) return@insertData

            put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, fullContact.firstName)
            put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, fullContact.lastName)

        }

        if (fullContact != null) {

            fullContact.nickName?.also {

                insertData(localId, ContactsContract.CommonDataKinds.Nickname.MIMETYPE) {
                    put(ContactsContract.CommonDataKinds.Nickname.NAME, it)
                    put(ContactsContract.CommonDataKinds.Nickname.TYPE, ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT)
                }

            }

            insetPhone(localId, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, fullContact.homeMobile)
            insetPhone(localId, ContactsContract.CommonDataKinds.Phone.TYPE_HOME, fullContact.homePhone)
            insetPhone(localId, ContactsContract.CommonDataKinds.Phone.TYPE_WORK, fullContact.businessPhone)
            insetPhone(localId, ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE, fullContact.businessMobile)

            insertEmail(localId, ContactsContract.CommonDataKinds.Email.TYPE_HOME, fullContact.homeEmail)
            insertEmail(localId, ContactsContract.CommonDataKinds.Email.TYPE_WORK, fullContact.businessEmail)
            insertEmail(localId, ContactsContract.CommonDataKinds.Email.TYPE_OTHER, fullContact.otherEmail)

        } else {

            // TODO: Store by short info

        }

    }

    private fun insertNewRawContact(id: Long): Long {

        val cv = ContentValues().apply {
            put(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
            put(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
            put(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
            put(CustomContact.RawContacts.REMOTE_ID, id)
        }

        return rawContactsClient.insert(cv).lastPathSegment.toLong()

    }

    private inline fun insertData(rawContactId: Long, mimeType: String, contentValues: ContentValues.() -> Unit) {

        val cv = ContentValues()
                .apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, mimeType)
                }
                .apply(contentValues)

        dataClient.insert(cv)

    }

    private fun insetPhone(rawContactId: Long, type: Int, number: String?) {
        number ?: return

        insertData(rawContactId, ContactsContract.CommonDataKinds.Phone.MIMETYPE) {
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            put(ContactsContract.CommonDataKinds.Phone.TYPE, type)
        }

    }

    private fun insertEmail(rawContactId: Long, type: Int, email: String?) {
        email ?: return

        insertData(rawContactId, ContactsContract.CommonDataKinds.Email.MIMETYPE) {
            put(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
            put(ContactsContract.CommonDataKinds.Email.TYPE, type)
        }

    }

    private fun getFirstLocal(): Single<List<ContactGroupInfo>> =
            repository.getContactsGroupsInfo(false)
                    .timeout(5, TimeUnit.SECONDS)
                    .firstOrError()

    private fun List<ContactGroupInfo>.isAllSyncEnabled(): Boolean =
            find { it.id == ContactsRepository.GROUP_ALL }?.syncing == true

    private fun isNotChanged(contact: RemoteContact): Boolean {

        return rawContactsClient.query(
                arrayOf(ContactsContract.RawContacts._ID),
                """
                    ${CustomContact.RawContacts.REMOTE_ID} = ${contact.id} AND
                    ${CustomContact.RawContacts.ETAG} LIKE '${contact.eTag}'
                """.trimIndent()
                )
                ?.letAndClose { it.count == 1 } ?: throw UnexpectedNullCursorException()

    }

    private fun getLocalContactId(remoteId: Long) : Long? {

        val cursor = rawContactsClient.query(
                arrayOf(ContactsContract.RawContacts._ID),
                "${CustomContact.RawContacts.REMOTE_ID} = $remoteId"
        ) ?: throw UnexpectedNullCursorException()

        return cursor.letAndClose { if (it.moveToFirst()) it.getLong(0) else null }

    }

    object DigestUtil {

        fun toSha256(source: String): String {

            return MessageDigest.getInstance("SHA-256")!!
                    .let {
                        it.update(source.toByteArray())
                        it.digest()
                    }.joinToString("") {
                        val hex = Integer.toHexString(0xFF and it.toInt()).toUpperCase()
                        if (hex.length == 2) hex else "0$hex"
                    }

        }

    }

}