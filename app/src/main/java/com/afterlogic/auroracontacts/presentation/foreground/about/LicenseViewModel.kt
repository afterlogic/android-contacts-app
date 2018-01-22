package com.afterlogic.auroracontacts.presentation.foreground.about

import android.databinding.Bindable
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.bindable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

class LicenseViewModel @Inject constructor(
        interactor: LicenseInteractor,
        subscriber: Subscriber
): ObservableRxViewModel(subscriber) {

    @get:Bindable
    var licenseText by bindable("")

    val title: Observable<String> by lazy { titleSubject }
    private val titleSubject = PublishSubject.create<String>()

    private val idPublisher = SingleSubject.create<Long>()

    init {

        idPublisher.observeOn(Schedulers.io())
                .flatMap { interactor.getLicense(it) }
                .defaultSchedulers()
                .subscribeIt {
                    licenseText = it.licenceText
                    titleSubject.onNext(it.library)
                }

    }

    fun initWith(id: Long) {
        idPublisher.onSuccess(id)
    }

}