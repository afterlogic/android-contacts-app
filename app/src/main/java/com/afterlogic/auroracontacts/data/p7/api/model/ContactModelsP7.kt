package com.afterlogic.auroracontacts.data.p7.api.model

import com.google.gson.annotations.SerializedName

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

data class ContactsGroupP7(
    @SerializedName("@Object")
    val objectType: String,
    @SerializedName("IdUser")
    val idUser: Long,
    @SerializedName("Id")
    val id: Long,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Email")
    val email: String? = null,
    @SerializedName("IsUseFriendlyName")
    val isUseFriendlyName: Boolean = false,
    @SerializedName("IsIsGroup")
    val isIsGroup: Boolean = false,
    @SerializedName("IsIsOrganization")
    val isIsOrganization: Boolean = false,
    @SerializedName("IsReadOnly")
    val isReadOnly: Boolean = false,
    @SerializedName("IsItsMe")
    val isItsMe: Boolean = false,
    @SerializedName("IsGlobal")
    val isGlobal: Boolean = false,
    @SerializedName("IsForSharedToAll")
    val isForSharedToAll: Boolean = false,
    @SerializedName("IsSharedToAll")
    val isSharedToAll: Boolean = false,
    @SerializedName("Frequency")
    val frequency: Int = 0,
    @SerializedName("AgeScore")
    val ageScore: Int = 0,
    @SerializedName("Emails")
    val emails: List<Any> = emptyList(),
    @SerializedName("Phones")
    val phones: List<Any> = emptyList()
)