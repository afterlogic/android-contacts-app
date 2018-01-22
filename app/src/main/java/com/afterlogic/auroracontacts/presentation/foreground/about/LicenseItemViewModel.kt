package com.afterlogic.auroracontacts.presentation.foreground.about

import com.afterlogic.auroracontacts.data.LicenseInfo

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
class LicenseItemViewModel(
        private val licenseInfo: LicenseInfo,
        private val onClick: (LicenseInfo) -> Unit
) {

    val author = licenseInfo.author
    val type = licenseInfo.type
    val library = licenseInfo.library

    fun onClick() {
        onClick(licenseInfo)
    }

}