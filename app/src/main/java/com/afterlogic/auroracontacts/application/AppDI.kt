package com.afterlogic.auroracontacts.application

import com.afterlogic.auroracontacts.core.CoreModule
import com.afterlogic.auroracontacts.data.DataModule
import com.afterlogic.auroracontacts.presentation.PresentationModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Component(modules = [
    CoreModule::class,
    DataModule::class,
    PresentationModule::class
])
interface AppComponent : AndroidInjector<App> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()

}