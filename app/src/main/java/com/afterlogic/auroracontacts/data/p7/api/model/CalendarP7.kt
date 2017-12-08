package com.afterlogic.auroracontacts.data.api.p7.model

import com.google.gson.annotations.SerializedName

data class CalendarP7(
        @SerializedName("Access")
    val access: Int = 0,
        @SerializedName("CTag")
    val cTag: String? = null,
        @SerializedName("Color")
    val color: String,
        @SerializedName("Description")
    val description: String?,
        @SerializedName("Etag")
    val eTag: String,
        @SerializedName("ExportHash")
    val exportHash: String? = null,
        @SerializedName("Id")
    val id: String,
        @SerializedName("IsDefault")
    val isDefault: Boolean = false,
        @SerializedName("IsPublic")
    val isPublic: Boolean = false,
        @SerializedName("Name")
    val name: String,
        @SerializedName("Owner")
    val owner: String,
        @SerializedName("PrincipalId")
    val principalId: String? = null,
        @SerializedName("PrincipalUrl")
    val principalUrl: String? = null,
        @SerializedName("PubHash")
    val pubHash: String? = null,
        @SerializedName("ServerUrl")
    val serverUrl: String? = null,
        @SerializedName("Shared")
    val isShared: Boolean = false,
        @SerializedName("SharedToAll")
    val isSharedToAll: Boolean = false,
        @SerializedName("SharedToAllAccess")
    val sharedToAllAccess: Int = 0,
        @SerializedName("Shares")
    val shares: List<Shares>? = null,
        @SerializedName("Url")
    val url: String? = null
) {

    data class Shares (
            @SerializedName("access")
            val access: Int = 0,
            @SerializedName("email")
            val email: String? = null,
            @SerializedName("name")
            val name: String? = null
    )

}