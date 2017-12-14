package com.afterlogic.auroracontacts.core.util

/**
 * Created by sunny on 14.12.2017.
 * mail: mail@sunnydaydev.me
 */

fun <T> MutableList<T>.setSequentiallyFrom(source: Collection<T>) {

    source.forEachIndexed { i, item ->

        if (lastIndex < i) {
            add(i, item)
        } else if (this[i] !== item) {
            this[i] = item
        }

    }

    removeAll { item -> source.find { it === item } == null }

}