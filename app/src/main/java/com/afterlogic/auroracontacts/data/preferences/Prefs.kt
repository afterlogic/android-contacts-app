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

    var calendarsFetched by booleanPrefs("calendarsFetched")

    private fun booleanPrefs(name: String, defaultValue: Boolean = false): ReadWriteProperty<Prefs, Boolean> {

        return object : ReadWriteProperty<Prefs, Boolean> {

            override fun getValue(thisRef: Prefs, property: KProperty<*>): Boolean {
                return prefs.getBoolean(name, defaultValue)
            }

            override fun setValue(thisRef: Prefs, property: KProperty<*>, value: Boolean) {
                prefs.put(name, value)
            }

        }

    }

}