package com.afterlogic.auroracontacts.presentation.foreground.about

import com.afterlogic.auroracontacts.data.licences.LicencesRepository
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
class LicenseInteractor @Inject constructor(
        private val repository: LicencesRepository
) {

    fun getLicense(id: Long) = repository[id]

}