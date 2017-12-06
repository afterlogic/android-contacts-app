package com.afterlogic.auroracontacts.presentation.common.base

import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

abstract class InjectionDaggerFragment<T: Any>: DaggerFragment() {

    @set:Inject
    internal lateinit var injection: T

    protected fun <R> injectable(provider: T.() -> R): Lazy<R> {
        return lazy { provider(injection) }
    }

}