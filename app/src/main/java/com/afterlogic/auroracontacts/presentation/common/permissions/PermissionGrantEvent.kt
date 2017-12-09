package com.afterlogic.auroracontacts.presentation.common.permissions

import android.content.pm.PackageManager

/**
 * Created by sashka on 12.09.16.
 *
 *
 * mail: sunnyday.development@gmail.com
 */
class PermissionGrantEvent(val requestCode: Int, val permissions: Array<String>, val grantResults: IntArray) {

    val isAllGranted: Boolean
        get() {
            return grantResults.none { it != PackageManager.PERMISSION_GRANTED }
        }

}
