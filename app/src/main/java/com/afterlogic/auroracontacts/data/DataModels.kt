package com.afterlogic.auroracontacts.data

import java.util.concurrent.TimeUnit

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

enum class SyncPeriod(val duration: Long) {

    OFF(-1),
    HOUR(TimeUnit.HOURS.toMillis(1)),
    SIX_HOURS(TimeUnit.HOURS.toMillis(6)),
    HALF_DAY(TimeUnit.HOURS.toMillis(12)),
    DAY(TimeUnit.DAYS.toMillis(1));

    companion object {

        fun byDuration(duration: Long): SyncPeriod? = values()
                .firstOrNull { duration == it.duration }

    }

}