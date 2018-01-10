package com.afterlogic.auroracontacts.data.contacts

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

data class RemoteContactGroup(
        val name: String,
        val id: Long
)

data class ContactGroupInfo(
        val name: String,
        val id: Long,
        val syncing: Boolean
)

interface RemoteContact {
    val userId: Long
    val id: Long
    val name: String
    val email: String?
    val isUseFriendlyName: Boolean
    val isIsGroup: Boolean
    val isIsOrganization: Boolean
    val isReadOnly: Boolean
    val isItsMe: Boolean
    val isGlobal: Boolean
    val isForSharedToAll: Boolean
    val isSharedToAll: Boolean
    val frequency: Int
    val ageScore: Int
    val emails: List<String>
    val phones: List<String>
}

data class RemoteFullContact (
    val birthdayDay: Int,
    val birthdayMonth: Int,
    val birthdayYear: Int,
    val businessCity: String?,
    val businessCompany: String?,
    val businessCountry: String?,
    val businessDepartment: String?,
    val businessEmail: String?,
    val businessFax: String?,
    val businessJobTitle: String?,
    val businessMobile: String?,
    val businessOffice: String?,
    val businessPhone: String?,
    val businessState: String?,
    val businessStreet: String?,
    val businessWeb: String?,
    val businessZip: String?,
    val facebook: String?,
    val firstName: String?,
    val fullName: String?,
    val isGlobal: Boolean,
    val groupsIds: List<String>?,
    val homeCity: String?,
    val homeCountry: String?,
    val homeEmail: String?,
    val homeFax: String?,
    val homeMobile: String?,
    val homePhone: String?,
    val homeState: String?,
    val homeStreet: String?,
    val homeWeb: String?,
    val homeZip: String?,
    val id: Long,
    val userId: Long,
    val isItsMe: Boolean,
    val lastName: String?,
    val nickName: String?,
    val notes: String?,
    val otherEmail: String?,
    val primaryEmail: Int,
    val isReadOnly: Boolean,
    val isSharedToAll: Boolean,
    val skype: String?,
    val title: String?,
    val isUseFriendlyName: Boolean
)