package com.afterlogic.auroracontacts.presentation.foreground.main

import com.afterlogic.auroracontacts.data.calendar.AuroraCalendar
import com.afterlogic.auroracontacts.data.calendar.CalendarsRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */
class MainInteractor @Inject constructor(
        private val calendarsRepository: CalendarsRepository
) {

    private val todoSyncPublisher = BehaviorSubject.createDefault(false)

    fun getCalendars(): Flowable<List<AuroraCalendar>> = calendarsRepository.getCalendars()

    fun listenSyncingState(): Observable<Boolean> = todoSyncPublisher

    fun requestStartSyncImmediately() : Completable {
        return Completable.timer(5, TimeUnit.SECONDS)
                .doOnSubscribe { todoSyncPublisher.onNext(true) }
                .doFinally { todoSyncPublisher.onNext(false) }
    }

}