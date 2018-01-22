package com.afterlogic.auroracontacts.presentation.foreground.unsuportedApi

import android.arch.lifecycle.ViewModel
import com.afterlogic.auroracontacts.presentation.common.base.MVVMInjection
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
@Module
abstract class UnsupportedApiDIModule {

    @Binds
    @IntoMap
    @ViewModelKey(UnsupportedApiViewModel::class)
    abstract fun bindViewModel(vm: UnsupportedApiViewModel): ViewModel

}

class UnsupportedApiInjection @Inject constructor(
        override val config: MVVMInjection.Config
): MVVMInjection