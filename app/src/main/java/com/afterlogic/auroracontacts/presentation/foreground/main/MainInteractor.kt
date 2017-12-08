package com.afterlogic.auroracontacts.presentation.foreground.main

import com.afterlogic.auroracontacts.data.calendar.AuroraCalendar
import com.afterlogic.auroracontacts.data.calendar.CalendarsRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class MainInteractor @Inject constructor(
        private val calendarsRepository: CalendarsRepository
) {

    fun getCalendars(): Single<List<AuroraCalendar>> = calendarsRepository.getCalendars()

}