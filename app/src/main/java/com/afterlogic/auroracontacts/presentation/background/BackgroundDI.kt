package com.afterlogic.auroracontacts.presentation.background

import com.afterlogic.auroracontacts.presentation.background.authenticator.AuthenticatorService
import com.afterlogic.auroracontacts.presentation.background.calendarsSync.CalendarsSyncService
import com.afterlogic.auroracontacts.presentation.background.syncStateService.SyncStateService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Module
abstract class BackgroundModule {

    @ContributesAndroidInjector
    abstract fun contributeCalendarSyncService(): CalendarsSyncService

    @ContributesAndroidInjector
    abstract fun controbuteSyncStateService(): SyncStateService

    @ContributesAndroidInjector
    abstract fun contributeAuthenticatorService(): AuthenticatorService

}