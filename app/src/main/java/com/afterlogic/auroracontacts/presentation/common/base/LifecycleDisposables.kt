package com.afterlogic.auroracontacts.presentation.common.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.rx.with
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class LifecycleDisposables @Inject constructor(
        private val subscriber: Subscriber
) : LifecycleObserver {

    enum class Scope(val value: Int) {
        NONE(0), CREATED(1), STARTED(2), RESUMED(3)
    }

    private val currentScope = BehaviorSubject.createDefault(Scope.NONE)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected fun onCreate() {
        currentScope.onNext(Scope.CREATED)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected fun onStart() {
        currentScope.onNext(Scope.STARTED)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected fun onResume() {
        currentScope.onNext(Scope.RESUMED)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected fun onPause() {
        currentScope.onNext(Scope.STARTED)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected fun onStop() {
        currentScope.onNext(Scope.CREATED)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected fun onDestroy() {
        currentScope.onNext(Scope.NONE)
    }

    fun <T> disposeByScope(scope: Scope?): Transformer<T> {
        val disposeScope = scope ?: currentScope.value
        return Transformer(disposeScope)
    }

    inner class Transformer<T>(
            private val scope: Scope
    ): CompletableTransformer, MaybeTransformer<T, T>, SingleTransformer<T, T>, ObservableTransformer<T, T> {

        override fun apply(upstream: Completable): CompletableSource {

            var scopeDisposable: Disposable? = null

            return upstream
                    .doOnSubscribe { disposable ->
                        scopeDisposable = currentScope
                                .map { it.value >= scope.value }
                                .with(subscriber)
                                .subscribe { if (!it) disposable.dispose() }
                    }
                    .doFinally { scopeDisposable?.dispose() }

        }

        override fun apply(upstream: Maybe<T>): MaybeSource<T> {

            var scopeDisposable: Disposable? = null

            return upstream
                    .doOnSubscribe { disposable ->
                        scopeDisposable = currentScope
                                .map { it.value >= scope.value }
                                .with(subscriber)
                                .subscribe { if (!it) disposable.dispose() }
                    }
                    .doFinally { scopeDisposable?.dispose() }

        }

        override fun apply(upstream: Single<T>): SingleSource<T> {

            var scopeDisposable: Disposable? = null

            return upstream
                    .doOnSubscribe { disposable ->
                        scopeDisposable = currentScope
                                .map { it.value >= scope.value }
                                .with(subscriber)
                                .subscribe { if (!it) disposable.dispose() }
                    }
                    .doFinally { scopeDisposable?.dispose() }

        }

        override fun apply(upstream: Observable<T>): ObservableSource<T> {

            var scopeDisposable: Disposable? = null

            return upstream
                    .doOnSubscribe { disposable ->
                        scopeDisposable = currentScope
                                .map { it.value >= scope.value }
                                .with(subscriber)
                                .subscribe { if (!it) disposable.dispose() }
                    }
                    .doFinally { scopeDisposable?.dispose() }

        }

    }

}