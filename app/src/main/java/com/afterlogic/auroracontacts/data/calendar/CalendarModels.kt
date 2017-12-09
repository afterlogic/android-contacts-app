package com.afterlogic.auroracontacts.data.calendar

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

data class RemoteCalendar(val id: String,
                          val name: String,
                          val description: String?,
                          val color: Int)

data class AuroraCalendar(val id: String,
                          val name: String,
                          val description: String?,
                          val color: Int,
                          val settings: AuroraCalendarSettings)

data class AuroraCalendarSettings(val syncEnabled: Boolean)


data class RemoteCalendarEvent(val id: String)

data class AuroraCalendarEvent(val id: String)