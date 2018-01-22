package com.afterlogic.auroracontacts.presentation.foreground.about

import android.arch.lifecycle.ViewModelProvider
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.LicencesFragmentBinding
import com.afterlogic.auroracontacts.presentation.common.FragmentCreator
import com.afterlogic.auroracontacts.presentation.common.base.MVVMFragment
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.inflateBinding
import javax.inject.Inject

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */

class LicencesFragment : MVVMFragment
<LicencesViewModel, LicencesFragmentBinding, LicencesInjection>() {

    class Creator @Inject constructor() : FragmentCreator {

        override fun create(seed: Any?): Fragment = LicencesFragment()

    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): LicencesFragmentBinding =
            inflater.inflateBinding(R.layout.about_fragment, container)

    override fun getViewModel(provider: ViewModelProvider): LicencesViewModel = provider.get()

}