package com.afterlogic.auroracontacts.presentation.foreground.mainActivity

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.core.util.IntentUtil
import com.afterlogic.auroracontacts.databinding.MainActivityBinding
import com.afterlogic.auroracontacts.presentation.FragmentTitleProvider
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fm = supportFragmentManager

        fm.addOnBackStackChangedListener {

            supportActionBar?.setDisplayHomeAsUpEnabled(
                    fm.backStackEntryCount > 0
            )

        }

        fm.registerFragmentLifecycleCallbacks(object: FragmentManager.FragmentLifecycleCallbacks() {

            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)
                title = when(f) {
                    is FragmentTitleProvider -> f.getFragmentTitle(this@MainActivity)
                    else -> getString(R.string.app_name)
                }
            }

        }, false)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }

        return true

    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigatorFactory.create(this))
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
    }

}