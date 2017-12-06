package com.afterlogic.auroracontacts.presentation.foreground.mainActivity

import android.arch.lifecycle.ViewModel
import com.afterlogic.auroracontacts.presentation.FragmentScope
import com.afterlogic.auroracontacts.presentation.common.base.MVVMInjection
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelKey
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginFragment
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginFragmentModule
import com.afterlogic.auroracontacts.presentation.foreground.main.MainFragment
import com.afterlogic.auroracontacts.presentation.foreground.main.MainFragmentModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import ru.terrakok.cicerone.NavigatorHolder
import javax.inject.Inject

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */


@Module
internal abstract class MainActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindViewModelToMap(vm: MainActivityViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector(modules = [LoginFragmentModule::class])
    abstract fun bindLogin(): LoginFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [MainFragmentModule::class])
    abstract fun bindMain(): MainFragment

}

class MainActivityInjection @Inject constructor(
        val navigationHolder: NavigatorHolder,
        override val config: MVVMInjection.Config
): MVVMInjection