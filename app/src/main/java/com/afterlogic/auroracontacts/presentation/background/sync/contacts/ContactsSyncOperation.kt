package com.afterlogic.auroracontacts.presentation.background.sync.contacts

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.core.util.cast
import com.afterlogic.auroracontacts.data.api.ApiNullResultError
import com.afterlogic.auroracontacts.data.contacts.ContactGroupInfo
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import com.afterlogic.auroracontacts.data.contacts.RemoteFullContact
import com.afterlogic.auroracontacts.data.sync.SyncRepository
import com.afterlogic.auroracontacts.presentation.background.sync.BaseSyncOperation
import com.afterlogic.auroracontacts.presentation.background.sync.CustomContract
import com.afterlogic.auroracontacts.presentation.background.sync.UnexpectedNullCursorException
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ContactsSyncOperation private constructor(
        private val requestBundle: Bundle,
        private val res: Resources,
        private val account: Account,
        contentClient: ContentProviderClient,
        private val repository: ContactsRepository,
        private val syncRepository: SyncRepository
) : BaseSyncOperation(account, contentClient) {

    class Factory @Inject constructor(
            private val res: Resources,
            private val repository: ContactsRepository,
            private val syncRepository: SyncRepository
    ) {

        fun create(account: Account,
                   client: ContentProviderClient,
                   requestBundle: Bundle) : ContactsSyncOperation {

            return ContactsSyncOperation(
                    requestBundle, res, account, client, repository, syncRepository
            )

        }

    }

    companion object {

        private val BIRTHDAY_FORMATTER: SimpleDateFormat get() = SimpleDateFormat("M/d/yy", Locale.US)

    }

    private val rawContactsClient = getContentClientHelper(ContactsContract.RawContacts.CONTENT_URI)
    private val dataClient = getContentClientHelper(ContactsContract.Data.CONTENT_URI)

    private val eTags = WeakHashMap<RemoteContact, String>()
    private val RemoteContact.eTag: String get() = eTags.getOrPut(this) {

        // TODO: replace reflection
        RemoteContact::class.java.methods
                .map { it.invoke(this) ?: "" }
                .joinToString(":")
                .let { DigestUtil.toSha256(it) }

    }

    fun sync() : Completable = checkIsNeedToSync().flatMapCompletable {

        if (it) {
            uploadLocalChanges()
                    .andThen(uploadDeletes())
                    .andThen(repository.loadRemote()).toCompletable() // Reload remote contacts groups
                    .andThen(getSyncableContactsGroups())
                    .flatMapCompletable {

                        val groupIds = it.map { it.id }

                        syncRemoteGroups(groupIds)

                    }
                    .doOnError(Timber::e)
        } else {
            Completable.complete()
        }

    }

    private fun checkIsNeedToSync() : Single<Boolean> {

        return if (syncRepository.isPeriodicallySync(requestBundle)) {
            syncRepository.periodicallySyncPeriod.map { it > 0L }
        } else {
            syncRepository.syncOnChanges
        }

    }

    private fun uploadLocalChanges() : Completable = Completable.defer {

        val c = rawContactsClient.query(
                arrayOf(ContactsContract.RawContacts._ID, CustomContract.Contacts.REMOTE_ID),
                """
                    ${ContactsContract.RawContacts.DIRTY} = 1 AND
                    ${ContactsContract.RawContacts.DELETED} = 0
                """.trimIndent()
        ) ?: throw UnexpectedNullCursorException()

        val ids: Map<Long, Long?> = mutableMapOf<Long, Long?>()
                .apply{

                    while (c.moveToNext()) {
                        this[c.getLong(0)] = c.getLong(CustomContract.Contacts.REMOTE_ID)
                    }

                }

        c.close()

        ids.map {

            val localId = it.key
            val remoteId = it.value

            val remoteContactData = collectRemoteContact(localId, remoteId)
            val new = remoteId == null

            val request =
                    if (new) uploadNewContact(remoteContactData)
                    else repository.updateContact(remoteContactData)

            request.doOnComplete {

                val contactCV = ContentValues().apply {
                    put(ContactsContract.RawContacts.DIRTY, 0)
                    put(CustomContract.Contacts.SYNCED, 1)
                }

                rawContactsClient.update(
                        contactCV, "${ContactsContract.RawContacts._ID} = $localId"
                ).also {
                    Timber.d("Local contact with rawId = $localId successfully updated.")
                }

            }

        } .let { Completable.concat(it) }

    }

    private fun uploadDeletes() : Completable = Completable.defer {

        val c = rawContactsClient.query(
                arrayOf(ContactsContract.RawContacts._ID, CustomContract.Contacts.REMOTE_ID),
                "${ContactsContract.RawContacts.DELETED} = 1"
        ) ?: throw UnexpectedNullCursorException()

        val ids: Map<Long, Long?> = mutableMapOf<Long, Long?>()
                .apply{

                    while (c.moveToNext()) {
                        this[c.getLong(0)] = c.getLong(CustomContract.Contacts.REMOTE_ID)
                    }

                }

        c.close()

        // Delete all not synced items
        ids.filter { (_, value) -> value == null } .also {

            val sqlIn = it.keys.map { it.toString() } .toSqlIn()

            rawContactsClient.delete("${ContactsContract.RawContacts._ID} IN $sqlIn")

        }

        // Delete remote items
        ids.filter { it.value != null } .mapValues { it.value!! } .map {

            val localId = it.key
            val remoteId = it.value

            repository.deleteContact(remoteId)
                    .doOnComplete {

                        rawContactsClient.delete(
                                "${ContactsContract.RawContacts._ID} = $localId"
                        )

                    }

        } .let { Completable.concat(it) }

    }

    private fun uploadNewContact(contact: RemoteFullContact) : Completable {

        return repository.getContactsGroupsInfo(false)
                .firstElement()
                .map { it.filter { it.id > 0 && it.syncing } }
                .flatMapCompletable {

                    val groupIds = it.map { it.id.toString() }
                    val newContact = contact.copy(groupsIds = groupIds)

                    repository.createContact(newContact)

                }

    }

    private fun syncRemoteGroups(groupIds: List<Long>): Completable {

        if (groupIds.isEmpty()) {

            return Completable.fromAction {

                rawContactsClient.delete("1")

            }

        } else {

            return syncGroupsRecursively(groupIds)
                    .doOnSuccess {

                        if (it.isEmpty()) {

                            rawContactsClient.delete(
                                    "${CustomContract.Contacts.SYNCED} = 1"
                            )

                        } else {

                            // Remote not exists in remote rawContactsClient
                            val idsSqlIn = it.map { it.toString() } .toSqlIn()

                            rawContactsClient.delete("""
                                ${CustomContract.Contacts.SYNCED} = 1 AND (
                                    ${CustomContract.Contacts.REMOTE_ID} IS NULL OR
                                    ${CustomContract.Contacts.REMOTE_ID} NOT IN $idsSqlIn
                                )
                            """.trimIndent())

                        }

                    }
                    .toCompletable()

        }

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
            dataClient.delete("${CustomContract.Contacts.REMOTE_ID} = ${contact.id}")
        }

        insertData(rawId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {

            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullContact?.fullName ?: contact.name)

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

            if (fullContact.facebook != null) {
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

            if (fullContact.birthdayDay != 0 && fullContact.birthdayMonth != 0 && fullContact.birthdayYear != 0) {

                insertData(rawId, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE) {

                    val date = GregorianCalendar().apply {
                        this[Calendar.DAY_OF_MONTH] = fullContact.birthdayDay
                        this[Calendar.MONTH] = fullContact.birthdayMonth - 1
                        this[Calendar.YEAR] = fullContact.birthdayYear
                    }.time

                    put(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
                    put(ContactsContract.CommonDataKinds.Event.START_DATE, BIRTHDAY_FORMATTER.format(date))

                }

            } else {

                insertData(rawId, CustomContract.Contacts.Birthday.CONTENT_ITEM_TYPE) {

                    put(CustomContract.Contacts.Birthday.DAY, fullContact.birthdayDay)
                    put(CustomContract.Contacts.Birthday.MONTH, fullContact.birthdayMonth)
                    put(CustomContract.Contacts.Birthday.YEAR, fullContact.birthdayYear)

                }

            }

            insertData(rawId, CustomContract.Contacts.Groups.CONTENT_ITEM_TYPE) {

                val groups = fullContact.groupsIds?.joinToString(",")
                put(CustomContract.Contacts.Groups.GROUPS, groups)

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
            put(ContactsContract.RawContacts.DIRTY, 0)
            put(CustomContract.Contacts.SYNCED, 1)
            put(CustomContract.Contacts.ETAG, contact.eTag)
        } .also { rawContactsClient.update(it, "${ContactsContract.RawContacts._ID} = $rawId") }

    }

    private fun insertNewRawContact(id: Long): Long {

        val cv = ContentValues().apply {
            put(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
            put(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
            put(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
            put(CustomContract.Contacts.REMOTE_ID, id)
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

    private fun getSyncableContactsGroups(): Single<List<ContactGroupInfo>> =
            repository.getContactsGroupsInfo(false)
                    .timeout(5, TimeUnit.SECONDS)
                    .firstOrError()
                    .map {

                        val syncable = it.filter { it.syncing }

                        syncable.find { it.id == ContactsRepository.GROUP_ALL }
                                ?.let { listOf(it) } ?: syncable

                    }

    private fun isNotChanged(contact: RemoteContact): Boolean {

        return rawContactsClient.query(
                arrayOf(ContactsContract.RawContacts._ID),
                """
                    ${CustomContract.Contacts.REMOTE_ID} = ${contact.id} AND
                    ${CustomContract.Contacts.ETAG} LIKE '${contact.eTag}'
                """.trimIndent()
                )
                ?.letAndClose { it.count == 1 } ?: throw UnexpectedNullCursorException()

    }

    private fun getRawContactId(remoteId: Long) : Long? {

        val cursor = rawContactsClient.query(
                arrayOf(ContactsContract.RawContacts._ID),
                "${CustomContract.Contacts.REMOTE_ID} = $remoteId"
        ) ?: throw UnexpectedNullCursorException()

        return cursor.letAndClose { if (it.moveToFirst()) it.getLong(0) else null }

    }

    private fun collectRemoteContact(rawContactId: Long, remoteId: Long? = null) : RemoteFullContact {

        val c = dataClient.query(selection = "${ContactsContract.Data.RAW_CONTACT_ID} = $rawContactId")
                ?: throw UnexpectedNullCursorException()

        val fields: Map<String, Any?> = {

            mutableMapOf<String, Any?>().apply {

                while (c.moveToNext()) {

                    putAll(collectRemoteFullContactFieldsFromData(c))

                }

            }

        }()

        c.close()

        return RemoteFullContact(
                fields[RemoteFullContact::birthdayDay.name] as Int? ?: 0,
                fields[RemoteFullContact::birthdayMonth.name] as Int? ?: 0,
                fields[RemoteFullContact::birthdayYear.name] as Int? ?: 0,
                fields[RemoteFullContact::businessCity.name] as String?,
                fields[RemoteFullContact::businessCompany.name] as String?,
                fields[RemoteFullContact::businessCountry.name] as String?,
                fields[RemoteFullContact::businessDepartment.name] as String?,
                fields[RemoteFullContact::businessEmail.name] as String?,
                fields[RemoteFullContact::businessFax.name] as String?,
                fields[RemoteFullContact::businessJobTitle.name] as String?,
                fields[RemoteFullContact::businessMobile.name] as String?,
                fields[RemoteFullContact::businessOffice.name] as String?,
                fields[RemoteFullContact::businessPhone.name] as String?,
                fields[RemoteFullContact::businessState.name] as String?,
                fields[RemoteFullContact::businessStreet.name] as String?,
                fields[RemoteFullContact::businessWeb.name] as String?,
                fields[RemoteFullContact::businessZip.name] as String?,
                fields[RemoteFullContact::facebook.name] as String?,
                fields[RemoteFullContact::firstName.name] as String?,
                fields[RemoteFullContact::fullName.name] as String?,
                false,
                fields[RemoteFullContact::groupsIds.name]?.cast() ?: emptyList(),
                fields[RemoteFullContact::homeCity.name] as String?,
                fields[RemoteFullContact::homeCountry.name] as String?,
                fields[RemoteFullContact::homeEmail.name] as String?,
                fields[RemoteFullContact::homeFax.name] as String?,
                fields[RemoteFullContact::homeMobile.name] as String?,
                fields[RemoteFullContact::homePhone.name] as String?,
                fields[RemoteFullContact::homeState.name] as String?,
                fields[RemoteFullContact::homeStreet.name] as String?,
                fields[RemoteFullContact::homeWeb.name] as String?,
                fields[RemoteFullContact::homeZip.name] as String?,
                remoteId ?: -1L,
                -1L,
                false,
                fields[RemoteFullContact::lastName.name] as String?,
                fields[RemoteFullContact::nickName.name] as String?,
                fields[RemoteFullContact::notes.name] as String?,
                fields[RemoteFullContact::otherEmail.name] as String?,
                0,
                false,
                false,
                fields[RemoteFullContact::skype.name] as String?,
                fields[RemoteFullContact::title.name] as String?,
                false
        )

    }

    private fun collectRemoteFullContactFieldsFromData(c: Cursor): Map<String, Any?> {

        val fields = mutableMapOf<String, Any?>()

        val mimeType = c.getString(ContactsContract.Data.MIMETYPE)

        when(mimeType) {

            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {

                fields[RemoteFullContact::fullName.name] = c.getString(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME)
                fields[RemoteFullContact::firstName.name] = c.getString(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
                fields[RemoteFullContact::lastName.name] = c.getString(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)

            }

            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE -> {
                fields[RemoteFullContact::nickName.name] = c.getString(ContactsContract.CommonDataKinds.Nickname.NAME)
            }

            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {

                val names = mapOf(
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE to RemoteFullContact::homeMobile.name,
                        ContactsContract.CommonDataKinds.Phone.TYPE_HOME to RemoteFullContact::homePhone.name,
                        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME to RemoteFullContact::homeFax.name,
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK to RemoteFullContact::businessPhone.name,
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE to RemoteFullContact::businessMobile.name,
                        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK to RemoteFullContact::businessFax.name
                )

                val type = c.getInt(ContactsContract.CommonDataKinds.Phone.TYPE)
                val name = names[type]!!

                fields[name] = c.getString(ContactsContract.CommonDataKinds.Phone.NUMBER)

            }

            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {

                val names = mapOf(
                        ContactsContract.CommonDataKinds.Email.TYPE_HOME to RemoteFullContact::homeEmail.name,
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK to RemoteFullContact::businessEmail.name,
                        ContactsContract.CommonDataKinds.Email.TYPE_OTHER to RemoteFullContact::otherEmail.name
                )

                val type = c.getInt(ContactsContract.CommonDataKinds.Email.TYPE)
                val name = names[type]!!

                fields[name] = c.getString(ContactsContract.CommonDataKinds.Email.ADDRESS)

            }

            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> {

                val type = c.getInt(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)

                val names = when(type) {

                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> mapOf(
                            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY to RemoteFullContact::homeCountry.name,
                            ContactsContract.CommonDataKinds.StructuredPostal.REGION to RemoteFullContact::homeState.name,
                            ContactsContract.CommonDataKinds.StructuredPostal.CITY to RemoteFullContact::homeCity.name,
                            ContactsContract.CommonDataKinds.StructuredPostal.STREET to RemoteFullContact::homeStreet.name,
                            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE to RemoteFullContact::homeZip.name
                    )

                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> mapOf(
                            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY to RemoteFullContact::businessCountry.name,
                            ContactsContract.CommonDataKinds.StructuredPostal.REGION to RemoteFullContact::businessState.name,
                            ContactsContract.CommonDataKinds.StructuredPostal.CITY to RemoteFullContact::businessCity.name,
                            ContactsContract.CommonDataKinds.StructuredPostal.STREET to RemoteFullContact::businessStreet.name,
                            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE to RemoteFullContact::businessZip.name
                    )

                    else -> null

                }

                names?.also {
                    it.map { it.value to c.getString(it.key) } .also { fields.putAll(it) }
                }

            }

            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE -> {

                val names = mapOf(
                        ContactsContract.CommonDataKinds.Website.TYPE_HOME to RemoteFullContact::homeWeb.name,
                        ContactsContract.CommonDataKinds.Website.TYPE_WORK to RemoteFullContact::businessWeb.name
                )

                val type = c.getInt(ContactsContract.CommonDataKinds.Email.TYPE)
                val name = names[type]!!

                fields[name] = c.getString(ContactsContract.CommonDataKinds.Website.URL)

            }

            ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE -> {

                val protocol = c.getInt(ContactsContract.CommonDataKinds.Im.PROTOCOL)

                val name = when(protocol) {
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE -> RemoteFullContact::skype.name
                    ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM -> {
                        val customType = c.getString(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL)
                        when(customType?.toLowerCase()) {
                            "fb", "facebook" -> RemoteFullContact::facebook.name
                            else -> null
                        }
                    }
                    else -> null
                }

                name?.also {
                    fields[name] = c.getString(ContactsContract.CommonDataKinds.Im.DATA)
                }

            }

            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> {

                val names = mapOf(
                        ContactsContract.CommonDataKinds.Organization.TITLE to RemoteFullContact::businessJobTitle.name,
                        ContactsContract.CommonDataKinds.Organization.DEPARTMENT to RemoteFullContact::businessDepartment.name,
                        ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION to RemoteFullContact::businessOffice.name
                )

                names.map { it.value to c.getString(it.key) } .also { fields.putAll(it) }

            }

            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE -> {
                fields[RemoteFullContact::notes.name] = c.getString(ContactsContract.CommonDataKinds.Note.NOTE)
            }

            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE -> {

                c.getString(ContactsContract.CommonDataKinds.Event.START_DATE)?.let {

                      try {
                          BIRTHDAY_FORMATTER.parse(it)
                      } catch (e: Throwable) {
                          null
                      }

                } ?.let {

                    GregorianCalendar().apply { time = it }

                } ?.also {

                    fields[RemoteFullContact::birthdayDay.name] = it[Calendar.DAY_OF_MONTH]
                    fields[RemoteFullContact::birthdayMonth.name] = it[Calendar.MONTH] + 1
                    fields[RemoteFullContact::birthdayYear.name] = it[Calendar.YEAR]

                }

            }

            CustomContract.Contacts.Birthday.CONTENT_ITEM_TYPE -> {

                fields[RemoteFullContact::birthdayDay.name] = c.getInt(CustomContract.Contacts.Birthday.DAY)
                fields[RemoteFullContact::birthdayMonth.name] = c.getInt(CustomContract.Contacts.Birthday. MONTH)
                fields[RemoteFullContact::birthdayYear.name] = c.getInt(CustomContract.Contacts.Birthday.YEAR)

            }

            CustomContract.Contacts.Groups.CONTENT_ITEM_TYPE -> {

                val groups = c.getString(CustomContract.Contacts.Groups.GROUPS)
                        ?.split(",")

                fields[RemoteFullContact::groupsIds.name] = groups

            }

        }

        return fields

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