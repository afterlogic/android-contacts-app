package com.afterlogic.auroracontacts.data.api.p7.model

import com.google.gson.annotations.SerializedName

data class CalendarEventP7(
    @SerializedName("Data")
    val data: String? = null,
    @SerializedName("ETag")
    val eTag: String? = null,
    @SerializedName("LastModified")
    val lastModified: String? = null,
    @SerializedName("Url")
    val url: String? = null
)