package com.afterlogic.auroracontacts.presentation.foreground.about

import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

class AboutViewModel @Inject constructor(
        subscriber: Subscriber
): ObservableRxViewModel(subscriber)