package com.afterlogic.auroracontacts.presentation.common.databinding

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.MapKey
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

class ViewModelFactory @Inject
constructor(
        private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(clazz: Class<T>): T {

        val creator = creators[clazz] ?: creators.entries
                .firstOrNull { clazz.isAssignableFrom(it.key) } ?.value
                ?: throw IllegalArgumentException("Unknown view model class: $clazz")

        @Suppress("UNCHECKED_CAST")
        return creator.get() as T

    }

}

inline fun <reified T: ViewModel> ViewModelProvider.get(): T {
    return get(T::class.java)
}

operator inline fun <reified T: ViewModel> ViewModelProvider.get(tag: String): T {
    return get(tag, T::class.java)
}
