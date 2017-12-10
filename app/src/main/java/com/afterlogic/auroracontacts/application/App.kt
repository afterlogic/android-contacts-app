package com.afterlogic.auroracontacts.application

import com.afterlogic.auroracontacts.core.util.Tagged
import com.afterlogic.auroracontacts.core.util.isMainProcess
import com.afterlogic.auroracontacts.presentation.common.base.InjectionDaggerApplication
import dagger.android.AndroidInjector
import timber.log.Timber
import javax.inject.Inject


/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */
class App: InjectionDaggerApplication<AppInjection>(), Tagged {

    private val activityTracker by inject { it.activityTracker.get() }

    private val loginStateDataController by inject { it.loginStateDataController.get() }

    @Inject
    fun logInjection() {
        Timber.d("Injecting $classTag")
    }

    override fun onCreate() {

        Timber.plant(Timber.DebugTree())

        super.onCreate()

        if (isMainProcess()) {

            activityTracker.register()

            loginStateDataController.start()

        }

    }

    override fun applicationInjector(): AndroidInjector<App> {
        return DaggerAppComponent.builder().create(this)
    }

}