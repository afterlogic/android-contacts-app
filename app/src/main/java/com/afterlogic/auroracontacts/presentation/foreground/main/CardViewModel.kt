package com.afterlogic.auroracontacts.presentation.foreground.main

import android.databinding.Bindable
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CardViewModel<out T>(val type: Int, val title: String, val items: List<T>)

open class ContactItemViewModel(
        val name: String,
        checkedInitialValue: Boolean,
        onCheckedChanged: (ContactItemViewModel, Boolean) -> Unit
) : ObservableViewModel() {

    @get:Bindable var checked by bindable(checkedInitialValue) { onCheckedChanged(this, it) }

}

class CalendarItemViewModel(
        val name: String,
        val color: Int,
        checkedInitialValue: Boolean,
        onCheckedChanged: (CalendarItemViewModel, Boolean) -> Unit
) : ObservableViewModel() {

    @get:Bindable var checked by bindable(checkedInitialValue) { onCheckedChanged(this, it) }

}