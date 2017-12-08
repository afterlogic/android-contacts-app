package com.afterlogic.auroracontacts.data.api.p7.model

import com.google.gson.annotations.SerializedName

data class CalendarEventP7(
    @SerializedName("Data")
    val data: String?,
    @SerializedName("ETag")
    val eTag: String,
    @SerializedName("LastModified")
    val lastModified: String,
    @SerializedName("Url")
    val url: String
)