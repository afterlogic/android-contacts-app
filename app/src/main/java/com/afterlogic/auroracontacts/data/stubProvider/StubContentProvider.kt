package com.afterlogic.auroracontacts.data.stubProvider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Created by sashka on 15.04.16.
 * mail: sunnyday.development@gmail.com
 */
class StubContentProvider : ContentProvider() {
    /*
     * Always return true, indicating that the
     * provider loaded correctly.
     */
    override fun onCreate(): Boolean {
        return true
    }

    /*
     * Return no type for MIME type
     */
    override fun getType(uri: Uri): String? {
        return null
    }

    /*
     * query() always returns no results
     *
     */
    override fun query(
            uri: Uri,
            projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?): Cursor? {
        return null
    }

    /*
     * insert() always returns null (no URI)
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    /*
     * delete() always returns "no rows affected" (0)
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    /*
     * update() always returns "no rows affected" (0)
     */
    override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<String>?): Int {
        return 0
    }
}