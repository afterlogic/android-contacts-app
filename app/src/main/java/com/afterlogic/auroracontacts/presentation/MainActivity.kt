package com.afterlogic.auroracontacts.presentation

import android.arch.lifecycle.ViewModelProvider
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.MainActivityBinding
import com.afterlogic.auroracontacts.presentation.common.base.MVVMActivity
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.setContentBinding

class MainActivity : MVVMActivity<MainViewModel, MainActivityBinding>() {

    override fun bindView(): MainActivityBinding = setContentBinding(R.layout.main_activity)

    override fun getViewModel(provider: ViewModelProvider): MainViewModel = provider.get()

}

class MainViewModel: ObservableViewModel() {

}
