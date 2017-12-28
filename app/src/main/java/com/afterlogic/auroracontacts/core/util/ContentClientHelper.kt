package com.afterlogic.auroracontacts.core.util

import android.content.ContentProviderClient
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class ContentClientHelper(private val client: ContentProviderClient, private val uri: Uri) {

    fun update(values: ContentValues, selection: String, selectionArgs: Array<String>? = null) {
        client.update(uri, values, selection, selectionArgs)
    }

    fun insert(values: ContentValues): Uri {
        return client.insert(uri, values)
    }

    fun query(projection: Array<String>? = null, selection: String = "1", selectionArgs: Array<String>? = null, sortOrder: String? = null): Cursor? {
        return client.query(uri, projection, selection, selectionArgs, sortOrder)
    }

    fun delete(selection: String, selectionArgs: Array<String>? = null): Int {
        return client.delete(uri, selection, selectionArgs)
    }

}