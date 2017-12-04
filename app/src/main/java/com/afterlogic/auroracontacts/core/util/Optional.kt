package com.afterlogic.auroracontacts.core.util

/**
 * Created by aleksandrcikin on 10.05.17.
 *
 */

open class Optional<T> {

    private var value: T? = null

    val isNotNull: Boolean
        get() = value != null

    constructor()

    constructor(value: T) {
        this.value = value
    }


    fun set(value: T?) {
        this.value = value
    }

    fun setAndGet(value: T?): T? {
        this.value = value
        return value
    }

    fun get(): T? {
        return value
    }

    fun ifPresent(consumer: (T) -> Unit) {
        value?.let(consumer)
    }

    fun clear() {
        set(null)
    }

}
