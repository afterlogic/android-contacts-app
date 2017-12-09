package com.afterlogic.auroracontacts.presentation.background

import com.afterlogic.auroracontacts.presentation.background.calendarsSync.CalendarsSyncService
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

}