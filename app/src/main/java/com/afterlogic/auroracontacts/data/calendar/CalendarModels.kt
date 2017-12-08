package com.afterlogic.auroracontacts.data.calendar

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

data class AuroraCalendar(val id: String,
                          val name: String,
                          val description: String?,
                          val color: Int)

data class AuroraCalendarEvent(val id: String)