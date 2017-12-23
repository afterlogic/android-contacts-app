package com.afterlogic.auroracontacts.presentation.foreground

import com.afterlogic.auroracontacts.presentation.foreground.mainActivity.MainActivity
import com.afterlogic.auroracontacts.presentation.foreground.mainActivity.MainActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import javax.inject.Scope

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Scope
annotation class ActivityScope

@Scope
annotation class FragmentScope

@Module
abstract class ForegroundModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeMainActivity(): MainActivity

}