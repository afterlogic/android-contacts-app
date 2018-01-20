package com.afterlogic.auroracontacts.core.util

import java.util.*

/**
 * Created by sunny on 27.12.2017.
 * mail: mail@sunnydaydev.me
 */
class StringIdProvider {

    private val ids = WeakHashMap<String, Long>()
    private var lastId = 0L

    operator fun get(stringId: String): Long = ids.getOrPut(stringId) { ++lastId }

}