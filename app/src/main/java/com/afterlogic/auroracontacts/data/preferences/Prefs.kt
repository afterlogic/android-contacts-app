package com.afterlogic.auroracontacts.data.preferences

import com.afterlogic.auroracontacts.application.App
import net.grandcentrix.tray.AppPreferences
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sunny on 09.12.2017.
 * mail: mail@sunnydaydev.me
 */

class Prefs @Inject constructor(context: App) {

    private val prefs = AppPreferences(context)

    var calendarsFetched by booleanPref("calendarsFetched")

    var contactsFetched by booleanPref("contactsFetched")

    var syncOnLocalChanges by booleanPref("syncOnLocalChanges")

    var syncPeriod by longPref("syncPeriod", -1L)

    var syncAllContacts by booleanPref("syncAllContacts")

    var syncSharedContacts by booleanPref("syncSharedContacts")

    var syncTeamContacts by booleanPref("syncTeamContacts")

    var syncPersonalContacts by booleanPref("syncPersonalContacts")

    var lastContactSyncCycleId by intPref("lastContactSyncCycleId")

    private fun booleanPref(name: String, defaultValue: Boolean = false): ReadWriteProperty<Prefs, Boolean> =
            property({ it.getBoolean(name, defaultValue) }, { p, v -> p.put(name, v) } )

    private fun longPref(name: String, defaultValue: Long = 0): ReadWriteProperty<Prefs, Long> =
            property({ it.getLong(name, defaultValue) }, { p, v -> p.put(name, v) } )

    private fun intPref(name: String, defaultValue: Int = 0): ReadWriteProperty<Prefs, Int> =
            property({ it.getInt(name, defaultValue) }, { p, v -> p.put(name, v) } )

    inline private fun <T> property(
            crossinline getter: (AppPreferences) -> T,
            crossinline setter: (AppPreferences, T) -> Unit
    ): ReadWriteProperty<Prefs, T> {

        return object : ReadWriteProperty<Prefs, T> {

            override fun getValue(thisRef: Prefs, property: KProperty<*>): T {
                return getter(prefs)
            }

            override fun setValue(thisRef: Prefs, property: KProperty<*>, value: T) {
                setter(prefs, value)
            }

        }

    }

}