package com.afterlogic.auroracontacts.presentation.foreground.main

import android.databinding.Bindable
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
class CardViewModel(val type: Int, val title: String, val items: List<ContactItemViewModel>)

open class ContactItemViewModel(
        val name: String,
        checked: Boolean,
        onCheckedChanged: (Boolean) -> Unit
) : ObservableViewModel() {

    @get:Bindable var checked by bindable(checked, onChanged = onCheckedChanged)

}

class CalendarItemViewModel(
        name: String,
        val color: Int,
        checked: Boolean,
        onCheckedChanged: (Boolean) -> Unit
) : ContactItemViewModel(name, checked, onCheckedChanged)