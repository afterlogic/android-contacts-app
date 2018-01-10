package com.afterlogic.auroracontacts.data.p7.contacts

import com.afterlogic.auroracontacts.data.contacts.RemoteFullContact
import javax.inject.Inject

/**
 * Created by sunny on 10.01.2018.
 * mail: mail@sunnydaydev.me
 */
class P7ContactsMapper @Inject constructor() {

    fun toPlain(source: P7RemoteFullContact): RemoteFullContact {
        return RemoteFullContact(
                source.birthdayDay,
                source.birthdayMonth,
                source.birthdayYear,
                source.businessCity,
                source.businessCompany,
                source.businessCountry,
                source.businessDepartment,
                source.businessEmail,
                source.businessFax,
                source.businessJobTitle,
                source.businessMobile,
                source.businessOffice,
                source.businessPhone,
                source.businessState,
                source.businessStreet,
                source.businessWeb,
                source.businessZip,
                source.facebook,
                source.firstName,
                source.fullName,
                source.isGlobal,
                source.groupsIds,
                source.homeCity,
                source.homeCountry,
                source.homeEmail,
                source.homeFax,
                source.homeMobile,
                source.homePhone,
                source.homeState,
                source.homeStreet,
                source.homeWeb,
                source.homeZip,
                source.idContact?.toLongOrNull() ?: -1L,
                source.userId,
                source.isItsMe,
                source.lastName,
                source.nickName,
                source.notes,
                source.otherEmail,
                source.primaryEmail,
                source.isReadOnly,
                source.isSharedToAll,
                source.skype,
                source.title,
                source.isUseFriendlyName
        )
    }

    fun toDto(source: RemoteFullContact): P7RemoteFullContact {
        return P7RemoteFullContact(
                source.birthdayDay,
                source.birthdayMonth,
                source.birthdayYear,
                source.businessCity,
                source.businessCompany,
                source.businessCountry,
                source.businessDepartment,
                source.businessEmail,
                source.businessFax,
                source.businessJobTitle,
                source.businessMobile,
                source.businessOffice,
                source.businessPhone,
                source.businessState,
                source.businessStreet,
                source.businessWeb,
                source.businessZip,
                source.facebook,
                source.firstName,
                source.fullName,
                source.isGlobal,
                source.groupsIds,
                source.homeCity,
                source.homeCountry,
                source.homeEmail,
                source.homeFax,
                source.homeMobile,
                source.homePhone,
                source.homeState,
                source.homeStreet,
                source.homeWeb,
                source.homeZip,
                source.id.takeIf { it != -1L } ?.toString(),
                source.userId,
                source.isItsMe,
                source.lastName,
                source.nickName,
                source.notes,
                source.otherEmail,
                source.primaryEmail,
                source.isReadOnly,
                source.isSharedToAll,
                source.skype,
                source.title,
                source.isUseFriendlyName
        )
    }

}