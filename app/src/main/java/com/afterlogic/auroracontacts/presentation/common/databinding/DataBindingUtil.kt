package com.afterlogic.auroracontacts.presentation.common.databinding

import android.app.Activity
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.ViewGroup
import com.afterlogic.auroracontacts.BR
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

fun <V: ViewDataBinding> Activity.setContentBinding(@LayoutRes layoutId: Int): V =
        DataBindingUtil.setContentView(this, layoutId)

fun <V: ViewDataBinding> LayoutInflater.inflateBinding(@LayoutRes layoutId: Int,
                                  root: ViewGroup?,
                                  attachToRoot: Boolean = false): V =
        DataBindingUtil.inflate(this, layoutId, root, attachToRoot)

class BindableDelegate<in R: ObservableViewModel, T: Any?>(
        private var value: T,private var id: Int?
): ReadWriteProperty<R, T> {

    companion object {
        private val namesMap = BR::class.java.fields.associate { it.name to it.getInt(null) }
    }

    override operator fun getValue(thisRef: R, property: KProperty<*>): T = this.value

    override operator fun setValue(thisRef: R, property: KProperty<*>, value: T) {

        this.value = value

        id = id ?: namesMap[property.name]

        thisRef.notifyPropertyChanged(id!!)

    }

}

fun <R: ObservableViewModel, T: Any?> bindable(initialValue: T, id: Int? = null):
        BindableDelegate<R, T> = BindableDelegate(initialValue, id)
