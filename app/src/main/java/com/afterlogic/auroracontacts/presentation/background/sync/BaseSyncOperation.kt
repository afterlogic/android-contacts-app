package com.afterlogic.auroracontacts.presentation.background.sync

import android.accounts.Account
import android.content.ContentProviderClient
import android.database.Cursor
import android.net.Uri
import com.afterlogic.auroracontacts.core.util.ContentClientHelper

/**
 * Created by sunny on 29.12.2017.
 * mail: mail@sunnydaydev.me
 */
abstract class BaseSyncOperation(
        private val account: Account,
        private val client: ContentProviderClient
) {

    protected fun getSyncUri(uri: Uri): Uri {

        return uri.buildUpon()
                .appendQueryParameter("caller_is_syncadapter", "true")
                .appendQueryParameter("account_name", account.name)
                .appendQueryParameter("account_type", account.type)
                .build()

    }

    protected fun getContentClientHelper(uri: Uri): ContentClientHelper {
        return ContentClientHelper(client, getSyncUri(uri))
    }

    protected fun List<String>.toSqlIn(): String = joinToString("', '", prefix = "('", postfix = "')")



    protected fun Cursor.getString(columnName: String, canBeBlank: Boolean = false) : String? {
        return getColumnIndex(columnName)
                .takeIf { it != -1 && !isNull(it) }
                ?.let { getString(it) }
                ?.takeIf { canBeBlank || it.isNotBlank() }
    }

    protected fun Cursor.getLong(columnName: String, canMinusOne: Boolean = false) : Long? {
        return getColumnIndex(columnName)
                .takeIf { it != -1 && !isNull(it) }
                ?.let { getLong(it) }
                ?.takeIf { canMinusOne || it != -1L }
    }

    protected fun Cursor.getInt(columnName: String, canMinusOne: Boolean = false) : Int? {
        return getColumnIndex(columnName)
                .takeIf { it != -1 && !isNull(it) }
                ?.let { getInt(it) }
                ?.takeIf { canMinusOne || it != -1 }
    }

}