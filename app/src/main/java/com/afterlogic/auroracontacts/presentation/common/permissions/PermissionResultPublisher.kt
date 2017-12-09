package ru.lipka.foodkeeper.presentation.common.modules.interactor.permissions

import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.presentation.common.permissions.PermissionGrantEvent
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by aleksandrcikin on 04.08.17.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class PermissionResultPublisher @Inject
internal constructor() {

    private val publisher = PublishSubject.create<PermissionGrantEvent>()

    fun onRequestPermissionResult(requestId: Int, permissions: Array<String>, results: IntArray) {
        publisher.onNext(PermissionGrantEvent(requestId, permissions, results))
    }

    internal fun listen(): Flowable<PermissionGrantEvent> {
        return publisher.toFlowable(BackpressureStrategy.LATEST)
    }

}
