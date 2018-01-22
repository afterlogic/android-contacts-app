package com.afterlogic.auroracontacts.presentation.foreground.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.MainFragmentBinding
import com.afterlogic.auroracontacts.presentation.common.FragmentCreator
import com.afterlogic.auroracontacts.presentation.common.base.MVVMFragment
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.inflateBinding
import javax.inject.Inject

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */

class MainFragment: MVVMFragment<MainViewModel, MainFragmentBinding, MainInjection>() {

    class Creator @Inject constructor() : FragmentCreator {

        override fun create(seed: Any?): Fragment = MainFragment()

    }

    private lateinit var logoutMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): MainFragmentBinding =
            inflater.inflateBinding(R.layout.main_fragment, container)

    override fun getViewModel(provider: ViewModelProvider): MainViewModel = provider.get()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main, menu)
        logoutMenuItem = menu.findItem(R.id.logout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout -> viewModel.onLogoutClicked()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        viewModel.logoutTitle.subscribeScoped {
            logoutMenuItem.title = it
        }

    }

}