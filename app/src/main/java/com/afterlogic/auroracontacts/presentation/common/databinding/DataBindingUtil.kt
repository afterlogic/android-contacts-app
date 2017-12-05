package com.afterlogic.auroracontacts.presentation.common.databinding

import android.app.Activity
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Handler
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

private val namesMap = BR::class.java.fields.associate { it.name to it.getInt(null) }

private fun getPropertyBrId(property: KProperty<*>): Int? {

    return property.name
            .let {

                namesMap[it] ?: {

                    if (it.startsWith("is")) {

                        it.substring(2)
                                .let { it.replace(it[0], it[0].toLowerCase()) }
                                .let { namesMap[it] }

                    } else {

                        null

                    }

                }()

            }
}

class BindableDelegate<in R: ObservableViewModel, T: Any?>(
        private var value: T,
        private val id: Int? = null,
        private val handler: Handler? = null,
        private  val onChanged: ((T) -> Unit)? = null
): ReadWriteProperty<R, T> {

    override operator fun getValue(thisRef: R, property: KProperty<*>): T = this.value

    override operator fun setValue(thisRef: R, property: KProperty<*>, value: T) {

        val changed = this.value != value

        this.value = value

        if (!changed) return

        val checkedId = id ?: getPropertyBrId(property) ?: throw IllegalArgumentException()

        if (handler == null || handler.looper?.thread === Thread.currentThread()) {

            thisRef.internalNotifyChanged(checkedId)

        } else {

            handler.post { thisRef.internalNotifyChanged(checkedId) }

        }

    }

    private fun R.internalNotifyChanged(id: Int) {

        notifyPropertyChanged(id)
        onChanged?.invoke(value)

    }

}

class BindableCommandDelegate<in R: ObservableViewModel, T: Any?>(
        private val id: Int? = null,
        private val handler: Handler? = null,
        private  val onFired: ((T) -> Unit)? = null
): ReadWriteProperty<R, T?> {

    private var value: T? = null

    override operator fun getValue(thisRef: R, property: KProperty<*>): T? {
        return value.also { value = null }
    }

    override operator fun setValue(thisRef: R, property: KProperty<*>, value: T?) {

        val changed = this.value != value

        this.value = value

        if (!changed) return

        value ?: return

        val checkedId = id ?: getPropertyBrId(property) ?: throw IllegalArgumentException()

        if (handler == null || handler.looper?.thread === Thread.currentThread()) {

            thisRef.internalNotifyChanged(checkedId, value)

        } else {

            handler.post { thisRef.internalNotifyChanged(checkedId, value) }

        }

    }

    private fun R.internalNotifyChanged(id: Int, value: T) {

        notifyPropertyChanged(id)
        onFired?.invoke(value)

    }

}

fun <R: ObservableViewModel, T: Any?> bindable(
        initialValue: T, id: Int? = null, handler: Handler? = null, onChanged: ((T) -> Unit)? = null
): BindableDelegate<R, T> = BindableDelegate(initialValue, id, handler, onChanged)

fun <R: ObservableViewModel, T: Any> bindableCommand(
        id: Int? = null, handler: Handler? = null, onFired: ((T) -> Unit)? = null
): BindableCommandDelegate<R, T> = BindableCommandDelegate(id, handler, onFired)
