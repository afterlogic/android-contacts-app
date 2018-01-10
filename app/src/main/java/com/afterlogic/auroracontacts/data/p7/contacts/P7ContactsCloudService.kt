package com.afterlogic.auroracontacts.data.p7.contacts

import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.p7.util.AuthConverterFactoryP7
import com.afterlogic.auroracontacts.data.api.p7.util.checkResponseAndGetData
import com.afterlogic.auroracontacts.data.contacts.ContactsRepository
import com.afterlogic.auroracontacts.data.p7.api.DynamicLazyApiP7
import com.afterlogic.auroracontacts.data.p7.api.P7ContactsApi
import com.afterlogic.auroracontacts.data.p7.common.AuthorizedService
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 27.12.2017.
 * mail: mail@sunnydaydev.me
 */

class P7ContactsCloudService @Inject constructor(
        dynamicLazyApiP7: DynamicLazyApiP7<P7ContactsApi>,
        accountService: AccountService,
        authConverterFactoryP7: AuthConverterFactoryP7
) : AuthorizedService<P7ContactsApi>(dynamicLazyApiP7, accountService, authConverterFactoryP7) {

    fun getContactsGroups(): Single<List<P7RemoteContact>> = api.flatMap { it.getContactsGroups() }
            .checkResponseAndGetData()

    fun getContacts(groupId: Long, offset: Int, limit: Int = 300) : Single<P7ContactsData> {

        val action = if (groupId == ContactsRepository.GROUP_TEAM) "ContactGlobalList" else "ContactList"
        val all = if (groupId == ContactsRepository.GROUP_ALL) 1 else null
        val shared = if (groupId == ContactsRepository.GROUP_SHARED) 1 else null
        val checkedGroupId = if (groupId >= 0) groupId else null

        return api.flatMap {
            it.getContacts(action, limit, offset, all, shared, shared, checkedGroupId)
        }
                .checkResponseAndGetData()

    }

    fun getFullContact(contactId: Long) : Single<P7RemoteFullContact> =
            api.flatMap { it.getFullContact(contactId) }
                    .checkResponseAndGetData()

    fun createContact(contact: P7RemoteFullContact) : Single<Boolean> = Single.defer {

        val fieldsMap = contact.toFieldsMap()
        api.flatMap { it.createContact(fieldsMap) }
                .checkResponseAndGetData()

    }

    fun updateContact(contact: P7RemoteFullContact) : Single<Boolean> = Single.defer {

        val fieldsMap = contact.toFieldsMap()
        api.flatMap { it.updateContact(contact.idContact!!.toLong(), fieldsMap) }
                .checkResponseAndGetData()

    }

    private fun P7RemoteFullContact.toFieldsMap() : Map<String, Any?> {

        val skipFields = arrayOf(
                "Global",
                "GroupsIds",
                "IdContact",
                "IdUser",
                "ItsMe",
                "PrimaryEmail",
                "ReadOnly",
                "SharedToAll",
                "UseFriendlyName"
        )

        val fields = P7RemoteFullContact::class.java.declaredFields

        //TODO: remove reflection

        return fields
                .filter { it.annotations.find { it is SerializedName } != null }
                .map { it to it.annotations.first { it is SerializedName } as SerializedName }
                .filterNot { (_, sn) -> skipFields.contains(sn.value) }
                .associate { (f, sn) ->
                    val accessible = f.isAccessible
                    f.isAccessible = true
                    (sn.value to f.get(this)).also { f.isAccessible = accessible }
                }
                .filterValues { it == null }

    }

}