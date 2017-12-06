package com.afterlogic.auroracontacts.presentation.foreground.main

import android.arch.lifecycle.ViewModel
import com.afterlogic.auroracontacts.presentation.common.base.MVVMInjection
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelKey
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject

/**
 * Created by sunny on 07.12.2017.
 * mail: mail@sunnydaydev.me
 */
@Module
abstract class MainFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindViewModel(vm: MainViewModel): ViewModel

}

class MainInjection @Inject constructor(
        override val config: MVVMInjection.Config
): MVVMInjection