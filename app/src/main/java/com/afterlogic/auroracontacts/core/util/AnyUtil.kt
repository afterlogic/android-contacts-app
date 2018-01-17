package com.afterlogic.auroracontacts.core.util

/**
 * Created by sunny on 17.01.2018.
 * mail: mail@sunnydaydev.me
 */

inline fun <reified T> Any.cast() : T? = takeIf { it is T } ?.let { it as T }