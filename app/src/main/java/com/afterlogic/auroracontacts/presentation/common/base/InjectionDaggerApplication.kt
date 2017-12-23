package com.afterlogic.auroracontacts.presentation.common.base

import dagger.android.support.DaggerApplication
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

abstract class InjectionDaggerApplication<T: Any>: DaggerApplication() {

    @set:Inject
    internal lateinit var injection: T

    protected fun <R> inject(provider: (T) -> R): Lazy<R> {
        return lazy { provider(injection) }
    }

}