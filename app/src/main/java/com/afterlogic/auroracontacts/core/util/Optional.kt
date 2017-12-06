package com.afterlogic.auroracontacts.core.util

/**
 * Created by aleksandrcikin on 10.05.17.
 *
 */

inline fun <T: Any, R: Any> T?.toOptional(map: (T) -> R): Optional<R> =
        this?.let { Optional(map(it)) } ?: Optional()

fun <T: Any> T?.toOptional(): Optional<T> = toOptional { it }

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
        value?.also(consumer)
    }

    fun clear() {
        set(null)
    }

}
