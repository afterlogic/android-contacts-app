package com.afterlogic.auroracontacts.application

import com.afterlogic.auroracontacts.core.util.Tagged
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject



/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */
class App: DaggerApplication(), Tagged {

    @Inject
    fun logInjection() {
        Timber.d("Injecting $classTag")
    }

    override fun onCreate() {

        Timber.plant(Timber.DebugTree())

        super.onCreate()

    }

    override fun applicationInjector(): AndroidInjector<App> {
        return DaggerAppComponent.builder().create(this)
    }

}