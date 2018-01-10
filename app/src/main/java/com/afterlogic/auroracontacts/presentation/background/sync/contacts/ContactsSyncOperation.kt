package com.afterlogic.auroracontacts.presentation.background.sync.contacts

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentUris
import android.content.ContentValues
import android.provider.ContactsContract
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.data.api.ApiNullResultError
import com.afterlogic.auroracontacts.data.contacts.ContactGroupInfo
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import com.afterlogic.auroracontacts.data.contacts.RemoteFullContact
import com.afterlogic.auroracontacts.presentation.background.sync.BaseSyncOperation
import com.afterlogic.auroracontacts.presentation.background.sync.CustomContract
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsSyncOperation private constructor(
        private val res: Resources,
        private val account: Account,
        contentClient: ContentProviderClient,
        private val repository: ContactsRepository
) : BaseSyncOperation(account, contentClient) {

    class Factory @Inject constructor(
            private val res: Resources,
            private val repository: ContactsRepository
    ) {

        fun create(account: Account, client: ContentProviderClient) : ContactsSyncOperation {
            return ContactsSyncOperation(res, account, client, repository)
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

                    if (it.isEmpty()) {

                        rawContactsClient.delete(
                                "${CustomContract.RawContacts.SYNCED} = 1"
                        )

                    } else {

                        // Remote not exists in remote rawContactsClient
                        val idsSqlIn = it.map { it.toString() } .toSqlIn()

                        rawContactsClient.delete("""
                            ${CustomContract.RawContacts.SYNCED} = 1 AND
                            ${CustomContract.RawContacts.REMOTE_ID} NOT IN $idsSqlIn
                        """.trimIndent())

                    }

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

        val (rawId, isNew) = getRawContactId(contact.id)?.let { it to false }
                ?: insertNewRawContact(contact.id) to true

        if (!isNew) {
            // Delete all current data
            dataClient.delete("${CustomContract.RawContacts.REMOTE_ID} = ${contact.id}")
        }

        insertData(rawId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {

            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)

            if (fullContact == null) return@insertData

            put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, fullContact.firstName)
            put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, fullContact.lastName)

        }

        if (fullContact != null) {

            fullContact.nickName?.also {

                insertData(rawId, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE) {
                    put(ContactsContract.CommonDataKinds.Nickname.NAME, it)
                    put(ContactsContract.CommonDataKinds.Nickname.TYPE, ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT)
                }

            }

            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, fullContact.homeMobile)
            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_HOME, fullContact.homePhone)
            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME, fullContact.homeFax)
            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_WORK, fullContact.businessPhone)
            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE, fullContact.businessMobile)
            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK, fullContact.businessFax)

            insertEmail(rawId, ContactsContract.CommonDataKinds.Email.TYPE_HOME, fullContact.homeEmail)
            insertEmail(rawId, ContactsContract.CommonDataKinds.Email.TYPE_WORK, fullContact.businessEmail)
            insertEmail(rawId, ContactsContract.CommonDataKinds.Email.TYPE_OTHER, fullContact.otherEmail)

            Address.instantiateOrNull(
                    fullContact.homeCountry, fullContact.homeState, fullContact.homeCity,
                    fullContact.homeStreet, fullContact.homeZip
            ).also { insertAddress(rawId, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME, it) }

            Address.instantiateOrNull(
                    fullContact.businessCountry, fullContact.businessState, fullContact.businessCity,
                    fullContact.businessStreet, fullContact.businessZip
            ).also { insertAddress(rawId, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK, it) }

            insertWebsite(rawId, ContactsContract.CommonDataKinds.Website.TYPE_HOME, fullContact.homeWeb)
            insertWebsite(rawId, ContactsContract.CommonDataKinds.Website.TYPE_WORK, fullContact.businessWeb)

            if (fullContact.skype != null) {
                insertData(rawId, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE) {
                    put(ContactsContract.CommonDataKinds.Im.DATA, fullContact.skype)
                    put(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE)
                }
            }

            if (fullContact.skype != null) {
                insertData(rawId, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE) {
                    put(ContactsContract.CommonDataKinds.Im.DATA, fullContact.facebook)
                    put(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM)
                    put(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, "Facebook")
                }
            }

            if (arrayOf(fullContact.businessJobTitle, fullContact.businessDepartment, fullContact.businessOffice).any { it != null }) {

                insertData(rawId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE) {

                    put(ContactsContract.CommonDataKinds.Organization.TITLE, fullContact.businessJobTitle)
                    put(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, fullContact.businessDepartment)
                    put(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION, fullContact.businessOffice)

                }

            }

            val noteParts = if (fullContact.isReadOnly) {
                arrayOf(res.strings[R.string.value_note_read_only], fullContact.notes)
            } else {
                arrayOf(fullContact.notes)
            }

            noteParts
                    .filterNot { it.isNullOrEmpty() }
                    .joinToString("\n")
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        insertData(rawId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE) {
                            put(ContactsContract.CommonDataKinds.Note.NOTE, it)
                        }
                    }

        } else {

            val phones = contact.phones.reversed()

            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, phones.getOrNull(0))
            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_HOME, phones.getOrNull(1))
            insetPhone(rawId, ContactsContract.CommonDataKinds.Phone.TYPE_WORK, phones.getOrNull(2))

            val homeEmail = contact.email ?: contact.emails.getOrNull(0)
            insertEmail(rawId, ContactsContract.CommonDataKinds.Email.TYPE_HOME, homeEmail)

            val otherEmails = contact.emails.filter { it == contact.email }
            insertEmail(rawId, ContactsContract.CommonDataKinds.Email.TYPE_WORK, otherEmails.getOrNull(0))
            insertEmail(rawId, ContactsContract.CommonDataKinds.Email.TYPE_OTHER, otherEmails.getOrNull(1))

            insertData(rawId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE) {
                put(ContactsContract.CommonDataKinds.Note.NOTE, res.strings[R.string.value_note_read_only])
            }

        }

        ContentValues().apply {
            put(ContactsContract.RawContacts.RAW_CONTACT_IS_READ_ONLY, fullContact == null || contact.isReadOnly)
            put(CustomContract.RawContacts.SYNCED, 1)
            put(CustomContract.RawContacts.ETAG, contact.eTag)
        } .also { rawContactsClient.update(it, "${ContactsContract.RawContacts._ID} = $rawId") }

    }

    private fun insertNewRawContact(id: Long): Long {

        val cv = ContentValues().apply {
            put(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
            put(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
            put(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
            put(CustomContract.RawContacts.REMOTE_ID, id)
        }

        return ContentUris.parseId(rawContactsClient.insert(cv))

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

        insertData(rawContactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            put(ContactsContract.CommonDataKinds.Phone.TYPE, type)
        }

    }

    private fun insertEmail(rawContactId: Long, type: Int, email: String?) {
        email ?: return

        insertData(rawContactId, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {
            put(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
            put(ContactsContract.CommonDataKinds.Email.TYPE, type)
        }

    }

    private fun insertAddress(rawContactId: Long, type: Int, address: Address?) {
        address ?: return

        insertData(rawContactId, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE) {

            put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, type)
            put(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, address.country)
            put(ContactsContract.CommonDataKinds.StructuredPostal.REGION, address.region)
            put(ContactsContract.CommonDataKinds.StructuredPostal.CITY, address.city)
            put(ContactsContract.CommonDataKinds.StructuredPostal.STREET, address.street)
            put(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, address.zip)

        }

    }

    private fun insertWebsite(rawContactId: Long, type: Int, address: String?) {
        address ?: return

        insertData(rawContactId, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE) {

            put(ContactsContract.CommonDataKinds.Website.URL, address)
            put(ContactsContract.CommonDataKinds.Website.TYPE, type)

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
                    ${CustomContract.RawContacts.REMOTE_ID} = ${contact.id} AND
                    ${CustomContract.RawContacts.ETAG} LIKE '${contact.eTag}'
                """.trimIndent()
                )
                ?.letAndClose { it.count == 1 } ?: throw UnexpectedNullCursorException()

    }

    private fun getRawContactId(remoteId: Long) : Long? {

        val cursor = rawContactsClient.query(
                arrayOf(ContactsContract.RawContacts._ID),
                "${CustomContract.RawContacts.REMOTE_ID} = $remoteId"
        ) ?: throw UnexpectedNullCursorException()

        return cursor.letAndClose { if (it.moveToFirst()) it.getLong(0) else null }

    }

    data class Address(val country: String?, val region: String?, val city: String?, val street: String?, val zip: String?) {

        companion object {

            fun instantiateOrNull(country: String?, region: String?, city: String?, street: String?, zip: String?) : Address? {

                if (arrayOf(country, region, city, street, zip).all { it == null }) return null

                return Address(country, region, city, street, zip)

            }

        }

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