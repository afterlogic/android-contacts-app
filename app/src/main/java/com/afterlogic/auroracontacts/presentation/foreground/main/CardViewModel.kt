package com.afterlogic.auroracontacts.presentation.foreground.main

import android.databinding.Bindable
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import com.github.nitrico.lastadapter.StableId

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

class CardViewModel<out T>(val type: Int, val title: String, val items: List<T>): StableId {
    override val stableId: Long = type.toLong()
}

open class ContactItemViewModel(
        val name: String,
        checkedInitialValue: Boolean,
        onCheckedChanged: (ContactItemViewModel, Boolean) -> Unit
) : ObservableViewModel() {

    @get:Bindable var checked by bindable(checkedInitialValue) { onCheckedChanged(this, it) }

}

class CalendarItemViewModel(
        override val stableId: Long,
        val name: String,
        val color: Int,
        checkedInitialValue: Boolean,
        onCheckedChanged: (CalendarItemViewModel, Boolean) -> Unit
) : ObservableViewModel(), StableId {

    @get:Bindable var checked by bindable(checkedInitialValue) { onCheckedChanged(this, it) }

}