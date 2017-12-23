package com.afterlogic.auroracontacts.presentation.common.base

import android.support.annotation.CallSuper
import com.afterlogic.auroracontacts.core.rx.*
import io.reactivex.*
import io.reactivex.disposables.Disposable

interface Subscribable {

    class Default(override val subscriber: Subscriber,
                  override val globalDisposable: DisposableBag): Subscribable

    val subscriber: Subscriber

    val globalDisposable: DisposableBag

    fun Completable.defaultSchedulers() : Completable =
            compose(subscriber::defaultSchedulers)

    fun <T> Maybe<T>.defaultSchedulers() : Maybe<T> =
            compose(subscriber::defaultSchedulers)

    fun <T> Single<T>.defaultSchedulers() : Single<T> =
            compose(subscriber::defaultSchedulers)

    fun <T> Observable<T>.defaultSchedulers() : Observable<T> =
            compose(subscriber::defaultSchedulers)

    fun <T> Flowable<T>.defaultSchedulers() : Flowable<T> =
            compose(subscriber::defaultSchedulers)

    fun Completable.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onComplete)

    }

    fun <T> Maybe<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onComplete, onSuccess)

    }

    fun <T> Single<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onSuccess: ResultHandler<T>? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onSuccess)

    }

    fun <T> Observable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onComplete, onNext)

    }

    fun <T> Flowable<T>.subscribeIt(
            onError: ErrorHandler? = null,
            onComplete: CompleteHandler? = null,
            onNext: ResultHandler<T>? = null
    ) : Disposable {

        return disposeBy(globalDisposable)
                .with(subscriber)
                .subscribe(onError, onComplete, onNext)

    }

    @CallSuper
    fun disposeAll() {
        globalDisposable.dispose()
    }

}