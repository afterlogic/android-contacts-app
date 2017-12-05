package com.afterlogic.auroracontacts.presentation

import com.afterlogic.auroracontacts.presentation.foreground.main.MainActivity
import com.afterlogic.auroracontacts.presentation.foreground.main.MainActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Scope

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Module(includes = [
    AndroidSupportInjectionModule::class
])
abstract class PresentationModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeMainActivity(): MainActivity

}

@Scope
annotation class AppScope

@Scope
annotation class ActivityScope

@Scope
annotation class FragmentScope