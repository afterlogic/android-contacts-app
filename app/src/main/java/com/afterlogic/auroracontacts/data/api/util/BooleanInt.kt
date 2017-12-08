package com.afterlogic.auroracontacts.data.api.util

data class BooleanInt(private val booleanValue: Boolean) {

    companion object {
        val TRUE = BooleanInt(true)
        val FALSE = BooleanInt(false)
    }

    val value: Int get() = if (booleanValue) 1 else 0

    override fun toString(): String = value.toString()

}