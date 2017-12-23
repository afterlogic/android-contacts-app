package com.afterlogic.auroracontacts.presentation.common.base

import android.support.annotation.CallSuper
import com.afterlogic.auroracontacts.core.rx.DisposableBag
import com.afterlogic.auroracontacts.core.rx.Subscriber

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
abstract class ObservableRxViewModel(
        subscriber: Subscriber
) : ObservableViewModel(), Subscribable by Subscribable.Default(subscriber, DisposableBag()) {

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        disposeAll()
    }

}