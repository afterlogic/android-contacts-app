package com.afterlogic.auroracontacts.presentation.foreground.login

import android.arch.lifecycle.ViewModel
import com.afterlogic.auroracontacts.presentation.common.base.MVVMInjection
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Module
abstract class LoginFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindViewModel(vm: LoginViewModel): ViewModel

}

class LoginInjection @Inject constructor(
        override val config: MVVMInjection.Config
): MVVMInjection