package com.afterlogic.auroracontacts.presentation.foreground.main

import android.arch.lifecycle.ViewModelProvider
import android.view.LayoutInflater
import android.view.ViewGroup
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.MainFragmentBinding
import com.afterlogic.auroracontacts.presentation.FragmentScope
import com.afterlogic.auroracontacts.presentation.common.base.MVVMFragment
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.inflateBinding
import javax.inject.Inject

class MainFragment : MVVMFragment<MainFragmentViewModel, MainFragmentBinding>() {

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): MainFragmentBinding =
            inflater.inflateBinding(R.layout.login_fragment, container)

    override fun getViewModel(provider: ViewModelProvider): MainFragmentViewModel = provider.get()

}

@FragmentScope
class MainFragmentViewModel @Inject constructor(mainViewModel: MainViewModel) : ObservableViewModel()
