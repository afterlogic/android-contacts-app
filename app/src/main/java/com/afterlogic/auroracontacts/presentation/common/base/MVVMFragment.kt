package com.afterlogic.auroracontacts.presentation.common.base

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afterlogic.auroracontacts.BR
import com.afterlogic.auroracontacts.core.util.Tagged
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelFactory

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */
abstract class MVVMFragment
<out VM: ObservableViewModel, VDB: ViewDataBinding, I: MVVMInjection> :
        InjectionDaggerFragment<I>(), Tagged, HasLifecycleDisposables {

    protected var viewModelKey = BR.vm

    private val viewModelFactory by inject { it.config.viewModelFactory }

    override val lifecycleDisposables by inject { it.config.lifecycleDisposables }

    override val subscriber by inject { it.config.subscriber }

    protected lateinit var binding: VDB
        private set

    protected val viewModel: VM by lazy {
        val provider = getViewModelProvider(viewModelFactory)
        getViewModel(provider)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleDisposables.classTag = this.toString().split(".").last()

        lifecycle.addObserver(viewModel)

        lifecycleDisposables.onCreate()

    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = bindView(inflater, container)
        binding.setVariable(viewModelKey, viewModel)
        binding.executePendingBindings()
        return binding.root

    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        lifecycleDisposables.onStart()
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        lifecycleDisposables.onResume()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        lifecycleDisposables.onPause()
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        lifecycleDisposables.onStop()
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        lifecycleDisposables.onDestroy()
    }

    abstract protected fun bindView(inflater: LayoutInflater,
                                    container: ViewGroup?): VDB

    abstract protected fun getViewModel(provider: ViewModelProvider): VM

    open protected fun getViewModelProvider(factory: ViewModelFactory): ViewModelProvider {
        return ViewModelProviders.of(this, factory)
    }

}