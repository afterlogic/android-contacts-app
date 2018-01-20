package com.afterlogic.auroracontacts.data.api.p7.model

import com.google.gson.annotations.SerializedName

/**
 * Created by sashka on 18.03.16.
 * mail: sunnyday.development@gmail.com
 */
data class AuthTokenP7 (

    @SerializedName("AuthToken")
    val token: String

)
