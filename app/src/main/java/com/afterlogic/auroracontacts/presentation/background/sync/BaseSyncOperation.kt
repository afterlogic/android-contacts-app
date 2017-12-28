package com.afterlogic.auroracontacts.presentation.background.sync

import android.accounts.Account
import android.content.ContentProviderClient
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

}