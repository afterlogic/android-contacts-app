package com.afterlogic.auroracontacts.data.p7.api.model

import com.google.gson.annotations.SerializedName

/**
 * Created by sunny on 16.01.2018.
 * mail: mail@sunnydaydev.me
 */

data class P7CreateContactResult(
    @SerializedName("IdContact")
    val idContact: String
)
