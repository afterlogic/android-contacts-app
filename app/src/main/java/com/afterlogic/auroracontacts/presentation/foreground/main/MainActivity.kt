package com.afterlogic.auroracontacts.presentation.foreground.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.MainActivityBinding
import com.afterlogic.auroracontacts.presentation.ActivityScope
import com.afterlogic.auroracontacts.presentation.common.base.MVVMActivity
import com.afterlogic.auroracontacts.presentation.common.base.ObservableViewModel
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.setContentBinding
import javax.inject.Inject

class MainActivity : MVVMActivity<MainViewModel, MainActivityBinding>() {

    override fun bindView(): MainActivityBinding = setContentBinding(R.layout.main_activity)

    override fun getViewModel(provider: ViewModelProvider): MainViewModel = provider.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
                .add(binding.content.id, MainFragment())
                .commitAllowingStateLoss()

        supportFragmentManager.beginTransaction()
                .add(binding.content.id, MainFragment())
                .commitAllowingStateLoss()

    }

}

@ActivityScope
class MainViewModel @Inject constructor() : ObservableViewModel()
