package com.afterlogic.auroracontacts.presentation.foreground.about

import com.afterlogic.auroracontacts.data.LicenseInfo
import com.afterlogic.auroracontacts.data.licences.LicencesRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
class LicensesInteractor @Inject constructor(
        private val repository: LicencesRepository
) {

    val licences: Single<List<LicenseInfo>> get() = repository.licences

}