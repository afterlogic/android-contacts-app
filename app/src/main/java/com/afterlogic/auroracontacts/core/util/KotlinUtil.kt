package com.afterlogic.auroracontacts.core.util

/**
 * Created by sunny on 20.01.2018.
 * mail: mail@sunnydaydev.me
 */

operator fun <T> Boolean.rem(block: () -> T) : T? = if (this) block() else null