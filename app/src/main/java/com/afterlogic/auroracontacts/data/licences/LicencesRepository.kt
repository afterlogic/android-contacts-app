package com.afterlogic.auroracontacts.data.licences

import com.afterlogic.auroracontacts.data.LicenseInfo
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
class LicencesRepository @Inject constructor(
        private val dao: LicencesDao
) {

    val licences: Single<List<LicenseInfo>> get() = dao.all.map { it.map(::toPlain) }

    operator fun get(id: Long): Single<LicenseInfo> = dao[id].map(::toPlain)

    private fun toPlain(source: LicenceDbe): LicenseInfo = LicenseInfo(
            source.id, source.library, source.author, source.type, source.text
    )

}