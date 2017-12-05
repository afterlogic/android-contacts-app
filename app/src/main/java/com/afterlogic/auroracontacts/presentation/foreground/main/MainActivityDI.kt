package com.afterlogic.auroracontacts.presentation.foreground.main

import android.arch.lifecycle.ViewModel
import com.afterlogic.auroracontacts.presentation.FragmentScope
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelKey
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginFragment
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginFragmentModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */


@Module
internal abstract class MainActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindViewModelToMap(vm: MainViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector(modules = [LoginFragmentModule::class])
    abstract fun bindLogin(): LoginFragment

}