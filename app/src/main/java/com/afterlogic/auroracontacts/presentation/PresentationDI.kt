package com.afterlogic.auroracontacts.presentation

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Module(includes = [AndroidSupportInjectionModule::class])
abstract class PresentationModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

}