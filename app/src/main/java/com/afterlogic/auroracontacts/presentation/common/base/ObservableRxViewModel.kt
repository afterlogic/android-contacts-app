package com.afterlogic.auroracontacts.presentation.common.base

import android.support.annotation.CallSuper
import com.afterlogic.auroracontacts.core.rx.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

/**
 * Created by sunny on 05.12.2017.
 * mail: mail@sunnydaydev.me
 */
abstract class ObservableRxViewModel(private val subscriber: Subscriber) : ObservableViewModel() {

    protected val globalDisposable = DisposableBag()

    protected fun Completable.defaultSchedulers() : Completable =
            compose(subscriber::defaultSchedulers)

    protected fun <T> Maybe<T>.defaultSchedulers() : Maybe<T> =
            compose(subscriber::defaultSchedulers)

    protected fun <T> Single<T>.defaultSchedulers() : Single<T> =
            compose(subscriber::defaultSchedulers)

    protected fun <T> Observable<T>.defaultSchedulers() : Observable<T> =
            compose(subscriber::defaultSchedulers)

    protected fun Completable.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onComplete)

    }

    protected fun <T> Maybe<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onComplete, onSuccess)

    }

    protected fun <T> Single<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onSuccess)

    }

    protected fun <T> Observable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onComplete, onNext)

    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()

        globalDisposable.dispose()

    }

}