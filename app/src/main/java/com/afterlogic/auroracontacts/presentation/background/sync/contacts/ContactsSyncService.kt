package com.afterlogic.auroracontacts.presentation.background.sync.contacts

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.IBinder
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.presentation.background.syncStateService.SyncStateHolder
import com.afterlogic.auroracontacts.presentation.common.base.InjectionDaggerService
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

class ContactsSyncService: InjectionDaggerService<ContactsSyncService.Injection>() {

    class Injection @Inject constructor(
            val adapter: ContactsSyncAdapter,
            val syncStateHolder: SyncStateHolder
    )

    private val syncAdapter by inject { it.adapter }
    private val stateHolder by inject { it.syncStateHolder }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        stateHolder.contactsSyncing.onNext(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        stateHolder.contactsSyncing.onNext(false)
    }

    override fun onBind(intent: Intent?): IBinder = syncAdapter.syncAdapterBinder

}

@AppScope
class ContactsSyncAdapter @Inject constructor(
        context: App,
        private val contactsSyncOperationFactory: ContactsSyncOperation.Factory
): AbstractThreadedSyncAdapter(context, true, false) {

    override fun onPerformSync(account: Account,
                               params: Bundle?,
                               authority: String,
                               contentProviderClient: ContentProviderClient,
                               syncResult: SyncResult?) {

        Timber.d("onPerformSync")

        contactsSyncOperationFactory.create(account, contentProviderClient)
                .sync()
                .blockingGet()
                ?.let(Timber::e)

        Timber.d("onPerformSyncEnd")

    }

}

