package com.afterlogic.auroracontacts.data

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.HOURS

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

private fun duration(duration: Long, unit: TimeUnit) : Long = unit.toSeconds(duration)

enum class SyncPeriod(val durationInSeconds: Long) {

    OFF(-1),
    HOUR(duration(1, HOURS)),
    SIX_HOURS(duration(6, HOURS)),
    HALF_DAY(duration(12, HOURS)),
    DAY(duration(1, DAYS));

    companion object {

        fun byDuration(duration: Long): SyncPeriod? = values()
                .firstOrNull { duration == it.durationInSeconds }

    }

}

data class LicenseInfo(val id: Long, val library: String, val author: String, val type: String, val licenceText: String)