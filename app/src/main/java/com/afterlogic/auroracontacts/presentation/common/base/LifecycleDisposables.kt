package com.afterlogic.auroracontacts.presentation.common.base

import com.afterlogic.auroracontacts.BuildConfig
import com.afterlogic.auroracontacts.core.rx.Subscriber
import com.afterlogic.auroracontacts.core.rx.with
import com.afterlogic.auroracontacts.core.util.Tagged
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class LifecycleDisposables @Inject constructor(
        private val subscriber: Subscriber
) : Tagged {

    enum class Scope(val value: Int) {
        DESTROYED(0), CREATED(1), STARTED(2), RESUMED(3);
    }

    override var classTag: String = ""
        set(value) {
            field = value
            tagSetted = true
        }

    private var tagSetted = false

    private val currentScope = BehaviorSubject.createDefault(Scope.DESTROYED)

    init {

        if (BuildConfig.DEBUG) {

            currentScope
                    .filter { tagSetted }
                    .distinctUntilChanged()
                    .with(subscriber)
                    .subscribe { Timber.d("$classTag: Active scope: $it") }

        }

    }

    internal fun onCreate() {
        currentScope.onNext(Scope.CREATED)
    }

    internal fun onStart() {
        currentScope.onNext(Scope.STARTED)
    }

    internal fun onResume() {
        currentScope.onNext(Scope.RESUMED)
    }

    internal fun onPause() {
        currentScope.onNext(Scope.STARTED)
    }

    internal fun onStop() {
        currentScope.onNext(Scope.CREATED)
    }

    internal fun onDestroy() {
        currentScope.onNext(Scope.DESTROYED)
    }

    fun <T> disposeByScope(scope: Scope?): Transformer<T> {

        val disposeScope = (scope ?: currentScope.value).let {
            if (it != Scope.DESTROYED) it
            else Scope.CREATED
        }

        return Transformer(disposeScope)

    }

    inner class Transformer<T>(
            private val scope: Scope
    ): CompletableTransformer, MaybeTransformer<T, T>, SingleTransformer<T, T>, ObservableTransformer<T, T> {

        override fun apply(upstream: Completable): CompletableSource {

            var scopeDisposable: Disposable? = null

            return upstream
                    .doOnSubscribe { scopeDisposable = onSubscribe(it) }
                    .doFinally { scopeDisposable?.dispose() }

        }

        override fun apply(upstream: Maybe<T>): MaybeSource<T> {

            var scopeDisposable: Disposable? = null

            return upstream
                    .doOnSubscribe { scopeDisposable = onSubscribe(it) }
                    .doFinally { scopeDisposable?.dispose() }

        }

        override fun apply(upstream: Single<T>): SingleSource<T> {

            var scopeDisposable: Disposable? = null

            return upstream
                    .doOnSubscribe { scopeDisposable = onSubscribe(it) }
                    .doFinally { scopeDisposable?.dispose() }

        }

        override fun apply(upstream: Observable<T>): ObservableSource<T> {

            var scopeDisposable: Disposable? = null

            return upstream
                    .doOnSubscribe { scopeDisposable = onSubscribe(it) }
                    .doFinally { scopeDisposable?.dispose() }

        }

        private fun onSubscribe(disposable: Disposable): Disposable {

            var handled = false

            return currentScope
                    .distinctUntilChanged()
                    .map { it.value >= scope.value }
                    .with(subscriber)
                    .subscribe {
                        if (it) {
                            handled = true
                        } else {
                            disposable.dispose()
                            if (!handled) {
                                Timber.e(LifecycleDisposablesDisposedImmediatelyError())
                            }
                        }
                    }

        }

    }

}

class LifecycleDisposablesDisposedImmediatelyError : Throwable()

interface HasLifecycleDisposables {

    val lifecycleDisposables: LifecycleDisposables
    val subscriber: Subscriber

    fun Completable.subscribeScoped(action: () -> Unit) {

        compose(subscriber::defaultSchedulers)
                .disposeByLifecycleScope()
                .with(subscriber)
                .subscribe(onComplete = action)

    }

    fun <T> Maybe<T>.subscribeScoped(action: (T) -> Unit) {

        compose(subscriber::defaultSchedulers)
                .disposeByLifecycleScope()
                .with(subscriber)
                .subscribe(onSuccess = action)

    }

    fun <T> Single<T>.subscribeScoped(action: (T) -> Unit) {

        compose(subscriber::defaultSchedulers)
                .disposeByLifecycleScope()
                .with(subscriber)
                .subscribe(onSuccess = action)

    }

    fun <T> Observable<T>.subscribeScoped(action: (T) -> Unit) {

        compose(subscriber::defaultSchedulers)
                .disposeByLifecycleScope()
                .with(subscriber)
                .subscribe(onNext = action)

    }

    fun Completable.disposeByLifecycleScope(scope: LifecycleDisposables.Scope? = null): Completable {
        return compose(lifecycleDisposables.disposeByScope<Any>(scope))
    }

    fun <T> Maybe<T>.disposeByLifecycleScope(scope: LifecycleDisposables.Scope? = null): Maybe<T> {
        return compose(lifecycleDisposables.disposeByScope(scope))
    }

    fun <T> Single<T>.disposeByLifecycleScope(scope: LifecycleDisposables.Scope? = null): Single<T> {
        return compose(lifecycleDisposables.disposeByScope(scope))
    }

    fun <T> Observable<T>.disposeByLifecycleScope(scope: LifecycleDisposables.Scope? = null): Observable<T> {
        return compose(lifecycleDisposables.disposeByScope(scope))
    }

}