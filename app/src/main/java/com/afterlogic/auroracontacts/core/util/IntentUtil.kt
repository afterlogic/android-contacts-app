package com.afterlogic.auroracontacts.core.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.afterlogic.auroracontacts.BuildConfig
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sashka on 23.11.16.
 *
 *
 * mail: sunnyday.development@gmail.com
 */
object IntentUtil {

    fun makeRestartTask(intent: Intent): Intent {
        val result = Intent.makeRestartActivityTask(intent.component)
        result.putExtras(intent)
        return result
    }

    inline fun <reified T: Context> intent() : Intent {
        return Intent()
                .setComponent(componentName(T::class.java))
    }

    fun componentName(klass: Class<*>): ComponentName =
            ComponentName(BuildConfig.APPLICATION_ID, klass.name)

}

fun intentString(name: String? = null): ReadWriteProperty<Intent, String?> {
    return intentProperty(name,
            { getStringExtra(it) },
            { n, v -> putExtra(n, v) }
    )
}

fun intentBoolean(name: String? = null): ReadWriteProperty<Intent, Boolean> {
    return intentProperty(name,
            { getBooleanExtra(it, false) },
            { n, v -> putExtra(n, v) }
    )
}

private inline fun <T> intentProperty(
        name: String? = null,
        crossinline get: Intent.(String) -> T,
        crossinline set: Intent.(String, T) -> Unit
): ReadWriteProperty<Intent, T> {

    return object : ReadWriteProperty<Intent, T> {

        override fun getValue(thisRef: Intent, property: KProperty<*>): T {
            return get(thisRef, name ?: property.name)
        }

        override fun setValue(thisRef: Intent, property: KProperty<*>, value: T) {
            if ((value as T?) == null) {
                thisRef.removeExtra(name)
            } else {
                set(thisRef, name ?: property.name, value)
            }
        }

    }

}
