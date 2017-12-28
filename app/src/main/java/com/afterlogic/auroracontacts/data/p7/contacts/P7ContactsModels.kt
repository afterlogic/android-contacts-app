package com.afterlogic.auroracontacts.data.p7.contacts

import com.afterlogic.auroracontacts.data.contacts.RemoteContact
import com.afterlogic.auroracontacts.data.contacts.RemoteFullContact
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
        override val emails: List<Any> = emptyList(),
        @SerializedName("Phones")
        override val phones: List<Any> = emptyList()
) : RemoteContact

data class P7RemoteFullContact(
        @SerializedName("BirthdayDay")
        override val birthdayDay: Int,
        @SerializedName("BirthdayMonth")
        override val birthdayMonth: Int,
        @SerializedName("BirthdayYear")
        override val birthdayYear: Int,
        @SerializedName("BusinessCity")
        override val businessCity: String?,
        @SerializedName("BusinessCompany")
        override val businessCompany: String?,
        @SerializedName("BusinessCountry")
        override val businessCountry: String?,
        @SerializedName("BusinessDepartment")
        override val businessDepartment: String?,
        @SerializedName("BusinessEmail")
        override val businessEmail:String?,
        @SerializedName("BusinessFax")
        override val businessFax: String?,
        @SerializedName("BusinessJobTitle")
        override val businessJobTitle: String?,
        @SerializedName("BusinessMobile")
        override val businessMobile: String?,
        @SerializedName("BusinessOffice")
        override val businessOffice: String?,
        @SerializedName("BusinessPhone")
        override val businessPhone: String?,
        @SerializedName("BusinessState")
        override val businessState: String?,
        @SerializedName("BusinessStreet")
        override val businessStreet: String?,
        @SerializedName("BusinessWeb")
        override val businessWeb: String?,
        @SerializedName("BusinessZip")
        override val businessZip: String?,
        @SerializedName("Facebook")
        override val facebook: String?,
        @SerializedName("FirstName")
        override val firstName: String?,
        @SerializedName("FullName")
        override val fullName: String?,
        @SerializedName("Global")
        override val isGlobal: Boolean,
        @SerializedName("GroupsIds")
        override val groupsIds: List<String>?,
        @SerializedName("HomeCity")
        override val homeCity: String?,
        @SerializedName("HomeCountry")
        override val homeCountry: String?,
        @SerializedName("HomeEmail")
        override val homeEmail: String?,
        @SerializedName("HomeFax")
        override val homeFax: String?,
        @SerializedName("HomeMobile")
        override val homeMobile: String?,
        @SerializedName("HomePhone")
        override val homePhone: String?,
        @SerializedName("HomeState")
        override val homeState: String?,
        @SerializedName("HomeStreet")
        override val homeStreet: String?,
        @SerializedName("HomeWeb")
        override val homeWeb: String?,
        @SerializedName("HomeZip")
        override val homeZip: String?,
        @SerializedName("IdContact")
        private val idContact: String?,
        @SerializedName("IdUser")
        override val idUser: Long = 0,
        @SerializedName("ItsMe")
        override val isItsMe: Boolean,
        @SerializedName("LastName")
        override val lastName:String?,
        @SerializedName("NickName")
        override val nickName:String?,
        @SerializedName("Notes")
        override val notes:String?,
        @SerializedName("OtherEmail")
        override val otherEmail:String?,
        @SerializedName("PrimaryEmail")
        override val primaryEmail:Int,
        @SerializedName("ReadOnly")
        override val isReadOnly:Boolean,
        @SerializedName("SharedToAll")
        override val isSharedToAll:Boolean,
        @SerializedName("Skype")
        override val skype:String?,
        @SerializedName("Title")
        override val title:String?,
        @SerializedName("UseFriendlyName")
        override val isUseFriendlyName:Boolean
) : RemoteFullContact {

    override val auroraContactId:Long = idContact?.toLongOrNull() ?: -1

}