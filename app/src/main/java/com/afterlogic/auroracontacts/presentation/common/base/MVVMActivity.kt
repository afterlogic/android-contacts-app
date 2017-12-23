package com.afterlogic.auroracontacts.presentation.common.base

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.annotation.CallSuper
import com.afterlogic.auroracontacts.BR
import com.afterlogic.auroracontacts.core.util.Tagged
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelFactory

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

abstract class MVVMActivity
<out VM: ObservableViewModel, out VDB: ViewDataBinding, I: MVVMInjection> :
        InjectionDaggerActivity<I>(), Tagged, HasLifecycleDisposables {

    protected var viewModelKey = BR.vm

    private val viewModelFactory by inject { it.config.viewModelFactory }

    private val permissionsPublisher by inject { it.config.permissionsPublisher }

    override val lifecycleDisposables by inject { it.config.lifecycleDisposables }

    override val subscriber by inject { it.config.subscriber }


    protected val binding: VDB by lazy { bindView() }

    protected val viewModel: VM by lazy {
        val provider = getViewModelProvider(viewModelFactory)
        getViewModel(provider)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleDisposables.classTag = this.toString().split(".").last()

        lifecycle.addObserver(viewModel)

        binding.setVariable(viewModelKey, viewModel)

        lifecycleDisposables.onCreate()

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsPublisher.onRequestPermissionResult(requestCode, permissions, grantResults)
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
    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
        lifecycleDisposables.onDestroy()
    }

    abstract protected fun bindView(): VDB

    abstract protected fun getViewModel(provider: ViewModelProvider): VM

    open protected fun getViewModelProvider(factory: ViewModelFactory): ViewModelProvider {
        return ViewModelProviders.of(this, factory)
    }

}