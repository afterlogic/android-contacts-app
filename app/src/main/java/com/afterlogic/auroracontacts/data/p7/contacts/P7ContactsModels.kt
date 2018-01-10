package com.afterlogic.auroracontacts.data.p7.contacts

import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import com.google.gson.annotations.SerializedName

/**
 * Created by sunny on 28.12.2017.
 * mail: mail@sunnydaydev.me
 */

data class P7ContactsData(
        @SerializedName("ContactCount")
        val contactCount: Int,
        @SerializedName("GroupId")
        val groupId: String?,
        @SerializedName("Search")
        val search: String?,
        @SerializedName("FirstCharacter")
        val firstCharacter: String?,
        @SerializedName("All")
        val isAll: Boolean,
        @SerializedName("List")
        val list: List<P7RemoteContact>
)

data class P7RemoteContact(
        @SerializedName("@Object")
        val objectType: String,
        @SerializedName("IdUser")
        override val userId: Long,
        @SerializedName("Id")
        override val id: Long,
        @SerializedName("Name")
        override val name: String,
        @SerializedName("Email")
        override val email: String? = null,
        @SerializedName("IsUseFriendlyName")
        override val isUseFriendlyName: Boolean = false,
        @SerializedName("IsIsGroup")
        override val isIsGroup: Boolean = false,
        @SerializedName("IsIsOrganization")
        override val isIsOrganization: Boolean = false,
        @SerializedName("IsReadOnly")
        override val isReadOnly: Boolean = false,
        @SerializedName("IsItsMe")
        override val isItsMe: Boolean = false,
        @SerializedName("IsGlobal")
        override val isGlobal: Boolean = false,
        @SerializedName("IsForSharedToAll")
        override val isForSharedToAll: Boolean = false,
        @SerializedName("IsSharedToAll")
        override val isSharedToAll: Boolean = false,
        @SerializedName("Frequency")
        override val frequency: Int = 0,
        @SerializedName("AgeScore")
        override val ageScore: Int = 0,
        @SerializedName("Emails")
        override val emails: List<String> = emptyList(),
        @SerializedName("Phones")
        override val phones: List<String> = emptyList()
) : RemoteContact

data class P7RemoteFullContact(
        @SerializedName("BirthdayDay")
        val birthdayDay: Int,
        @SerializedName("BirthdayMonth")
        val birthdayMonth: Int,
        @SerializedName("BirthdayYear")
        val birthdayYear: Int,
        @SerializedName("BusinessCity")
        val businessCity: String?,
        @SerializedName("BusinessCompany")
        val businessCompany: String?,
        @SerializedName("BusinessCountry")
        val businessCountry: String?,
        @SerializedName("BusinessDepartment")
        val businessDepartment: String?,
        @SerializedName("BusinessEmail")
        val businessEmail:String?,
        @SerializedName("BusinessFax")
        val businessFax: String?,
        @SerializedName("BusinessJobTitle")
        val businessJobTitle: String?,
        @SerializedName("BusinessMobile")
        val businessMobile: String?,
        @SerializedName("BusinessOffice")
        val businessOffice: String?,
        @SerializedName("BusinessPhone")
        val businessPhone: String?,
        @SerializedName("BusinessState")
        val businessState: String?,
        @SerializedName("BusinessStreet")
        val businessStreet: String?,
        @SerializedName("BusinessWeb")
        val businessWeb: String?,
        @SerializedName("BusinessZip")
        val businessZip: String?,
        @SerializedName("Facebook")
        val facebook: String?,
        @SerializedName("FirstName")
        val firstName: String?,
        @SerializedName("FullName")
        val fullName: String?,
        @SerializedName("Global")
        val isGlobal: Boolean,
        @SerializedName("GroupsIds")
        val groupsIds: List<String>?,
        @SerializedName("HomeCity")
        val homeCity: String?,
        @SerializedName("HomeCountry")
        val homeCountry: String?,
        @SerializedName("HomeEmail")
        val homeEmail: String?,
        @SerializedName("HomeFax")
        val homeFax: String?,
        @SerializedName("HomeMobile")
        val homeMobile: String?,
        @SerializedName("HomePhone")
        val homePhone: String?,
        @SerializedName("HomeState")
        val homeState: String?,
        @SerializedName("HomeStreet")
        val homeStreet: String?,
        @SerializedName("HomeWeb")
        val homeWeb: String?,
        @SerializedName("HomeZip")
        val homeZip: String?,
        @SerializedName("IdContact")
        val idContact: String?,
        @SerializedName("IdUser")
        val userId: Long = 0,
        @SerializedName("ItsMe")
        val isItsMe: Boolean,
        @SerializedName("LastName")
        val lastName:String?,
        @SerializedName("NickName")
        val nickName:String?,
        @SerializedName("Notes")
        val notes:String?,
        @SerializedName("OtherEmail")
        val otherEmail:String?,
        @SerializedName("PrimaryEmail")
        val primaryEmail:Int,
        @SerializedName("ReadOnly")
        val isReadOnly:Boolean,
        @SerializedName("SharedToAll")
        val isSharedToAll:Boolean,
        @SerializedName("Skype")
        val skype:String?,
        @SerializedName("Title")
        val title:String?,
        @SerializedName("UseFriendlyName")
        val isUseFriendlyName:Boolean
)

