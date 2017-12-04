package com.afterlogic.auroracontacts.presentation.common.di

import com.afterlogic.auroracontacts.R
import javax.inject.Inject
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */
class Injectable<R, T> @Inject constructor(private val value: T): ReadOnlyProperty<R, T>{

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        return value
    }

}