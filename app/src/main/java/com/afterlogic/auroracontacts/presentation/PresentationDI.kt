package com.afterlogic.auroracontacts.presentation

import com.afterlogic.auroracontacts.presentation.background.BackgroundModule
import com.afterlogic.auroracontacts.presentation.foreground.ForegroundModule
import dagger.Module
import dagger.android.support.AndroidSupportInjectionModule

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Module(includes = [
    AndroidSupportInjectionModule::class,
    BackgroundModule::class,
    ForegroundModule::class
])
abstract class PresentationModule