package com.afterlogic.auroracontacts.presentation.foreground.main

import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
class MainViewModel @Inject constructor(
        subscriber: Subscriber
): ObservableRxViewModel(subscriber)  {

    val cards: ObservableList<CardViewModel> = ObservableArrayList()

}