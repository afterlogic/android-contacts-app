package com.afterlogic.auroracontacts.presentation.common.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

/**
 * Created by aleksandrcikin on 04.08.17.
 * mail: mail@sunnydaydev.me
 */

fun Context.checkSelfPermission(vararg permissions: PermissionRequest): Boolean {

    return permissions.none {

        it.permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

    }

}

enum class PermissionRequest constructor(val requestCode: Int, val permissions: Array<String>) {

    CALENDAR(1, arrayOf(
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR
    )),

    CONTACTS(2, arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    )),

    CALENDAR_AND_CONTACTS(3, CALENDAR.permissions + CONTACTS.permissions)

}
