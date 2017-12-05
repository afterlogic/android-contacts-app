package com.afterlogic.auroracontacts.data.api.p7.model

import com.google.gson.annotations.SerializedName

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */
class ApiResponseP7<T>{

    companion object {
        const val NAME_ERROR_CODE = "ErrorCode"
        const val NAME_ERROR_MESSAGE = "ErrorMessage"
        const val NAME_RESULT = "Result"
    }

    @field:SerializedName("AccountID")
    val accountId: Long? = -1

    @field:SerializedName(NAME_RESULT)
    val data: T? = null

    @field:SerializedName(NAME_ERROR_CODE)
    val errorCode: Int? = null

    @field:SerializedName(NAME_ERROR_MESSAGE)
    val errorMessage: String? = null

    val isSuccess: Boolean
        get() = errorCode == null && data != null

}