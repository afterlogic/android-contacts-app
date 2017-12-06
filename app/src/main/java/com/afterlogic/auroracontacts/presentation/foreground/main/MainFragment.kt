package com.afterlogic.auroracontacts.presentation.foreground.main

import android.arch.lifecycle.ViewModelProvider
import android.view.LayoutInflater
import android.view.ViewGroup
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.MainFragmentBinding
import com.afterlogic.auroracontacts.presentation.common.base.MVVMFragment
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.inflateBinding

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

class MainFragment: MVVMFragment<MainViewModel, MainFragmentBinding, MainInjection>() {

    companion object {

        fun instance(): MainFragment = MainFragment()

    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): MainFragmentBinding =
            inflater.inflateBinding(R.layout.main_fragment, container)

    override fun getViewModel(provider: ViewModelProvider): MainViewModel = provider.get()

}