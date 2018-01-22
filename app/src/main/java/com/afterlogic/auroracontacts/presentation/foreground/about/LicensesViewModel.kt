package com.afterlogic.auroracontacts.presentation.foreground.about

import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.data.LicenseInfo
import com.afterlogic.auroracontacts.presentation.common.base.ObservableRxViewModel
import com.afterlogic.auroracontacts.presentation.navigation.AppRouter
import com.afterlogic.auroracontacts.presentation.navigation.Screens
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

class LicensesViewModel @Inject constructor(
        private val router: AppRouter,
        interactor: LicensesInteractor,
        subscriber: Subscriber
): ObservableRxViewModel(subscriber) {

    val licenses: ObservableList<LicenseItemViewModel> = ObservableArrayList()

    init {

        interactor.licences
                .defaultSchedulers()
                .subscribeIt {

                    it.map { LicenseItemViewModel(it, this::onItemClick) }
                            .also {
                                licenses.clear()
                                licenses.addAll(it)
                            }

                }

    }

    private fun onItemClick(license: LicenseInfo) {
        router.navigateTo(Screens.LICENCE, license.id)
    }

}