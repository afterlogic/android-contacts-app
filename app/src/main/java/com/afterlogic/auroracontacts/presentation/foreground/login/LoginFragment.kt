package com.afterlogic.auroracontacts.presentation.foreground.login

import android.arch.lifecycle.ViewModelProvider
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.databinding.LoginFragmentBinding
import com.afterlogic.auroracontacts.presentation.common.FragmentCreator
import com.afterlogic.auroracontacts.presentation.common.base.MVVMFragment
import com.afterlogic.auroracontacts.presentation.common.databinding.get
import com.afterlogic.auroracontacts.presentation.common.databinding.inflateBinding
import javax.inject.Inject

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
class LoginFragment: MVVMFragment<LoginViewModel, LoginFragmentBinding, LoginInjection>() {

    class Creator @Inject constructor() : FragmentCreator {

        override fun create(seed: Any?) : Fragment = LoginFragment()

    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): LoginFragmentBinding =
            inflater.inflateBinding(R.layout.login_fragment, container)

    override fun getViewModel(provider: ViewModelProvider): LoginViewModel = provider.get()

}