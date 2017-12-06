package com.afterlogic.auroracontacts.presentation.common.base

import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelFactory
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

interface MVVMInjection {

    val config: Config

    data class Config @Inject constructor(
            val viewModelFactory: ViewModelFactory,
            val lifecycleDisposables: LifecycleDisposables,
            val subscriber: Subscriber
    )

}