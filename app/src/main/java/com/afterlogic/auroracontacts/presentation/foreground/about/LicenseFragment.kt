package com.afterlogic.auroracontacts.presentation.foreground.about

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.LicenceFragmentBinding
import com.afterlogic.auroracontacts.presentation.common.FragmentCreator
import com.afterlogic.auroracontacts.presentation.common.FragmentObservableTitleProvider
import com.afterlogic.auroracontacts.presentation.common.base.MVVMFragment
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.inflateBinding
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

class LicenseFragment : MVVMFragment
<LicenseViewModel, LicenceFragmentBinding, LicenceInjection>(), FragmentObservableTitleProvider {

    companion object {
        private const val KEY_ID = "id"
    }

    class Creator @Inject constructor() : FragmentCreator {

        override fun create(seed: Any?): Fragment = LicenseFragment().apply {
            arguments = Bundle().apply {
                putLong(KEY_ID, seed as Long)
            }
        }

    }

    override fun getFragmentTitle(): Observable<String> = viewModel.title

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): LicenceFragmentBinding =
            inflater.inflateBinding(R.layout.licence_fragment, container)

    override fun getViewModel(provider: ViewModelProvider): LicenseViewModel = provider.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initWith(arguments!!.getLong(KEY_ID))

    }

}