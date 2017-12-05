package com.afterlogic.auroracontacts.data.api.p7.model

import com.google.gson.annotations.SerializedName

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */
data class LoginRequestP7(
        @SerializedName("Email") val email: String,
        @SerializedName("IncPassword") val password: String
)