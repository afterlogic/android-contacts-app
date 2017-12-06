package com.afterlogic.auroracontacts.application

import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.util.Tagged
import com.afterlogic.auroracontacts.data.account.AccountService
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject



/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */
class App: DaggerApplication(), Tagged {

    @set:Inject
    lateinit var accountService: AccountService

    @set:Inject
    lateinit var subscriber: Subscriber

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