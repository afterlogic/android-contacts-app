package com.afterlogic.auroracontacts.presentation.foreground.main

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.core.util.IntentUtil
import com.afterlogic.auroracontacts.databinding.MainActivityBinding
import com.afterlogic.auroracontacts.presentation.common.base.MVVMActivity
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.setContentBinding
import com.afterlogic.auroracontacts.presentation.foreground.login.LoginFragment

class MainActivity : MVVMActivity<MainViewModel, MainActivityBinding, MainActivityInjection>() {

    companion object {

        fun intent(): Intent = IntentUtil.intent<MainActivity>()

    }

    override fun bindView(): MainActivityBinding = setContentBinding(R.layout.main_activity)

    override fun getViewModel(provider: ViewModelProvider): MainViewModel = provider.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
                .add(binding.content.id, LoginFragment.instance())
                .commitAllowingStateLoss()

    }

}