package com.afterlogic.auroracontacts.data.calendar

import com.afterlogic.auroracontacts.data.api.ApiType
import com.afterlogic.auroracontacts.data.api.ApiTypeKey
import com.afterlogic.auroracontacts.data.p7.calendars.CalendarsRemoteServiceP7
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */
@Module
abstract class CalendarDataModule {

    @Binds
    @IntoMap
    @ApiTypeKey(ApiType.P7)
    abstract fun bindP7RemoteService(p7: CalendarsRemoteServiceP7) : CalendarRemoteService

}