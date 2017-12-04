package com.afterlogic.auroracontacts.presentation.common.base

import android.arch.lifecycle.*
import android.databinding.ViewDataBinding
import android.os.Bundle
import com.afterlogic.auroracontacts.BR
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.rx.with
import com.afterlogic.auroracontacts.presentation.common.databinding.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.*
import javax.inject.Inject

/**
 * Created by sunny on 04.12.2017.
 * mail: mail@sunnydaydev.me
 */

abstract class MVVMActivity<VM: ObservableViewModel, VDB: ViewDataBinding>: DaggerAppCompatActivity() {

    protected var viewModelKey = BR.vm

    @set:Inject
    protected lateinit var viewModelFactory: ViewModelFactory

    @set:Inject
    protected lateinit var lifecycleDisposables: LifecycleDisposables

    @set:Inject
    protected lateinit var subscriber: Subscriber

    protected val binding: VDB by lazy { bindView() }

    protected val viewModel: VM by lazy {
        val provider = getViewModelProvider(viewModelFactory)
        getViewModel(provider)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(lifecycleDisposables)

        binding.setVariable(viewModelKey, viewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }

    abstract protected fun bindView(): VDB

    abstract protected fun getViewModel(provider: ViewModelProvider): VM

    open protected fun getViewModelProvider(factory: ViewModelFactory): ViewModelProvider {
        return ViewModelProviders.of(this, factory)
    }

    protected fun Completable.disposeByLifecycle(scope: LifecycleDisposables.Scope? = null): Completable {
        return compose(lifecycleDisposables.disposeByScope<Any>(scope))
    }

    protected fun <T> Maybe<T>.disposeByLifecycle(scope: LifecycleDisposables.Scope? = null): Maybe<T> {
        return compose(lifecycleDisposables.disposeByScope(scope))
    }

    protected fun <T> Single<T>.disposeByLifecycle(scope: LifecycleDisposables.Scope? = null): Single<T> {
        return compose(lifecycleDisposables.disposeByScope(scope))
    }

    protected fun <T> Observable<T>.disposeByLifecycle(scope: LifecycleDisposables.Scope? = null): Observable<T> {
        return compose(lifecycleDisposables.disposeByScope(scope))
    }

    protected fun Completable.subscribeDefault(action: () -> Unit) {

        compose(subscriber::defaultSchedulers)
                .disposeByLifecycle()
                .with(subscriber)
                .subscribe(onComplete = action)

    }

    protected fun <T> Maybe<T>.subscribeDefault(action: (T) -> Unit) {

        compose(subscriber::defaultSchedulers)
                .disposeByLifecycle()
                .with(subscriber)
                .subscribe(onSuccess = action)

    }

    protected fun <T> Single<T>.subscribeDefault(action: (T) -> Unit) {

        compose(subscriber::defaultSchedulers)
                .disposeByLifecycle()
                .with(subscriber)
                .subscribe(onSuccess = action)

    }

    protected fun <T> Observable<T>.subscribeDefault(action: (T) -> Unit) {

        compose(subscriber::defaultSchedulers)
                .disposeByLifecycle()
                .with(subscriber)
                .subscribe(onNext = action)

    }

}