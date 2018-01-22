package com.afterlogic.auroracontacts.presentation.background.appStarter

import com.afterlogic.auroracontacts.application.ActivityTracker
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.rx.with
import com.afterlogic.auroracontacts.core.util.isMainProcess
import com.afterlogic.auroracontacts.data.sync.SyncRepository
import com.afterlogic.auroracontacts.presentation.background.loginStateController.LoginStateController
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

class AppStarter @Inject constructor(
        private val app: App,
        private val activityTracker: ActivityTracker,
        private val loginStateController: LoginStateController,
        private val syncRepository: SyncRepository,
        private val subscriber: Subscriber
) {

    fun onAppReady() {

        if (app.isMainProcess()) {

            onReadyAtMainProcess()

        }

    }

    private fun onReadyAtMainProcess() {

        activityTracker.start()
        loginStateController.start()

        syncRepository.evaluateSyncSettings()
                .compose(subscriber::defaultSchedulers)
                .doOnError(Timber::d)
                .with(subscriber)
                .subscribe()

    }

}