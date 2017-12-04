package com.afterlogic.auroracontacts.core.util

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

interface Tagged {
    val classTag: String get() = this::class.java.name
}