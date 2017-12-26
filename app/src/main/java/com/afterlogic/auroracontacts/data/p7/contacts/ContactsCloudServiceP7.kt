package com.afterlogic.auroracontacts.data.p7.contacts

import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.p7.util.AuthConverterFactoryP7
import com.afterlogic.auroracontacts.data.api.p7.util.checkResponseAndGetData
import com.afterlogic.auroracontacts.data.p7.api.ContactsApiP7
import com.afterlogic.auroracontacts.data.p7.api.DynamicLazyApiP7
import com.afterlogic.auroracontacts.data.p7.api.model.ContactsGroupP7
import com.afterlogic.auroracontacts.data.p7.common.AuthorizedService
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 27.12.2017.
 * mail: mail@sunnydaydev.me
 */

class ContactsCloudServiceP7 @Inject constructor(
        dynamicLazyApiP7: DynamicLazyApiP7<ContactsApiP7>,
        accountService: AccountService,
        authConverterFactoryP7: AuthConverterFactoryP7
) : AuthorizedService<ContactsApiP7>(dynamicLazyApiP7, accountService, authConverterFactoryP7) {

    fun getContactsGroups(): Single<List<ContactsGroupP7>> = api.flatMap { it.getContactsGroups() }
            .checkResponseAndGetData()

}