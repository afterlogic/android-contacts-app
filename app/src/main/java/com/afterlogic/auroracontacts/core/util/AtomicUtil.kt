package com.afterlogic.auroracontacts.core.util

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

fun AtomicBoolean.compareAndSet(newValue: Boolean, ifChanged: () -> Unit = {}): Boolean {

    val changed = compareAndSet(!newValue, newValue)

    if (changed) ifChanged()

    return changed

}