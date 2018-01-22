package com.afterlogic.auroracontacts.presentation.foreground.mainActivity

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.core.util.IntentUtil
import com.afterlogic.auroracontacts.databinding.MainActivityBinding
import com.afterlogic.auroracontacts.presentation.common.base.MVVMActivity
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.setContentBinding

class MainActivity : MVVMActivity<MainActivityViewModel, MainActivityBinding, MainActivityInjection>() {

    companion object {

        fun intent(): Intent = IntentUtil.intent<MainActivity>()

    }

    private val navigationHolder by inject { it.navigationHolder }

    private val navigatorFactory by inject { it.navigatorFactory }

    override fun bindView(): MainActivityBinding = setContentBinding(R.layout.main_activity)

    override fun getViewModel(provider: ViewModelProvider): MainActivityViewModel = provider.get()

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigatorFactory.create(this))
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
    }

}