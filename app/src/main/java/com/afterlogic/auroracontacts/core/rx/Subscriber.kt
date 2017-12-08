package com.afterlogic.auroracontacts.core.rx

import com.afterlogic.auroracontacts.BuildConfig
import com.afterlogic.auroracontacts.application.wrappers.Toaster
import com.afterlogic.auroracontacts.core.util.isAnyNetworkError
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Created by aleksandrcikin on 25.04.17.
 *
 */

class Subscriber @Inject constructor(private val toaster: Toaster) {

    private val classPackage = this::class.java.name.split(".")
            .dropLast(1)
            .joinToString(".")

    fun <T> subscribe(
            onSubscribe: ((Disposable) -> Unit)? = null,
            onResult: ((T) -> Unit)? = null,
            onError: ((Throwable) -> Boolean)? = null,
            onComplete: (() -> Unit)? = null): AnyObserver<T> {

        val line = Thread.currentThread().stackTrace
                .dropWhile { !isSubscriberLog(it) }
                .dropWhile(this::isSubscriberLog)
                .first()

        val errorHandler: (Throwable) -> Unit = lambda@ {

            if (onError?.invoke(it) == true) return@lambda

            onUnhandledError(UnhandledError(line, it))

        }

        return AnyObserver(onSubscribe, onResult, onComplete, errorHandler)
    }

    fun defaultSchedulers(completable: Completable): Completable {
        return completable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> defaultSchedulers(maybe: Maybe<T>): Maybe<T> {
        return maybe.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> defaultSchedulers(single: Single<T>): Single<T> {
        return single.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> defaultSchedulers(observable: Observable<T>): Observable<T> {
        return observable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> defaultSchedulers(upstream: Flowable<T>): Flowable<T> {
        return upstream.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun onUnhandledError(e: UnhandledError) {

        val cause = e.cause?.let {
            if (it is ShadowedError) it.cause
            else it
        }

        if (cause == null) {
            Timber.e(e)
            return
        }

        when {

            cause.isAnyNetworkError -> { return }

            else -> {

                Timber.d(e)

                if(BuildConfig.DEBUG) {
                    toaster.showShort(e.message ?: "Произошла ошибка")
                }

            }

        }

    }

    inner class AnyObserver<T> internal constructor(
            private val subscribe: ((Disposable) -> Unit)?,
            private val result: ((T) -> Unit)?,
            private val complete: (() -> Unit)?,
            private val error: ((Throwable) -> Unit)?
    ) : CompletableObserver, MaybeObserver<T>, SingleObserver<T>, Observer<T>,
            org.reactivestreams.Subscriber<T> {

        private var completeHandled = AtomicBoolean(false)

        override fun onSuccess(t: T) {
            result?.invoke(t)
            handleComplete()
        }

        override fun onNext(t: T) {
            result?.invoke(t)
        }

        override fun onSubscribe(subscription: Subscription) {

            subscribe?.invoke(object : Disposable {

                internal var disposed = false

                override fun dispose() {

                    if (disposed) {
                        return
                    }
                    disposed = true

                    subscription.cancel()

                }

                override fun isDisposed(): Boolean = disposed

            })

        }

        override fun onSubscribe(d: Disposable) {
            subscribe?.invoke(d)
        }

        override fun onComplete() {
            handleComplete()
        }

        override fun onError(e: Throwable) {
            error?.invoke(e)
        }

        private fun handleComplete() {
            if (completeHandled.getAndSet(true)) return
            complete?.invoke()
        }

    }

    private fun isSubscriberLog(element: StackTraceElement): Boolean =
            element.fileName == "Subscriber.kt" || element.fileName == "Subscribable.kt" ||
                    element.fileName == "ObservableRxViewModel"

    inner class UnhandledError(
            line: StackTraceElement, cause: Throwable
    ): Throwable(
            "Unhandled subscriber error: ${cause.message}",
            cause, true, true
    ) {

        init {
            stackTrace = arrayOf(line)
        }

    }

}

abstract class ShadowedError(cause: Throwable): Throwable(
        if (cause is ShadowedError) cause.cause else cause
)

typealias ErrorHandler = (Throwable) -> Boolean
typealias ResultHandler<T> = (T) -> Unit
typealias CompleteHandler = () -> Unit

class SuccessErrorHandler(private val action: (Throwable) -> Unit): ErrorHandler {

    override fun invoke(p1: Throwable): Boolean {
        action(p1)
        return true
    }

}

class FailErrorHandler(private val action: (Throwable) -> Unit): ErrorHandler {

    override fun invoke(p1: Throwable): Boolean {
        action(p1)
        return false
    }

}

private fun <T> Subscriber.subscribe(onResult: ResultHandler<T>? = null,
                                     onError: ErrorHandler? = null,
                                     onComplete: CompleteHandler? = null,
                                     action: (Subscriber.AnyObserver<T>) -> Unit): Disposable {
    val disposable = ReDisposable()

    val observer = subscribe(
            { disposable.disposable = it },
            onResult,
            onError,
            onComplete
    )

    action(observer)

    return disposable.disposable!!
}

//region// Completable

fun Completable.with(subscriber: Subscriber) : CompletableSubscribtioning =
        CompletableSubscribtioning(this, subscriber)

class CompletableSubscribtioning(private val source: Completable,
                                 private val subscriber: Subscriber) {

    fun subscribe(onError: ErrorHandler? = null, onComplete: CompleteHandler? = null) : Disposable =
            subscriber.subscribe<Any>(null, onError, onComplete) { source.subscribe(it) }

}

fun Completable.subscribeWith(subs: Subscriber): Disposable = with(subs).subscribe()

inline fun Completable.shadowError(crossinline mapper: (Throwable) -> ShadowedError): Completable {
    return onErrorResumeNext { it: Throwable ->
        Completable.error(mapper(it))
    }
}

inline fun Completable.shadowErrorIfNot(crossinline mapper: (Throwable) -> ShadowedError): Completable {
    return onErrorResumeNext { it: Throwable ->
        if (it is ShadowedError) Completable.error(it)
        else Completable.error(mapper(it))
    }
}

//endregion

//region// Maybe

fun <T> Maybe<T>.with(subscriber: Subscriber) : MaybeSubscribtioning<T> =
        MaybeSubscribtioning(this, subscriber)

class MaybeSubscribtioning<out T>(private val source: Maybe<T>,
                                  private val subscriber: Subscriber) {

    fun subscribe(onError: ErrorHandler? = null, onComplete: CompleteHandler? = null,
                  onSuccess: ResultHandler<T>? = null) : Disposable =
            subscriber.subscribe(onSuccess, onError, onComplete) { source.subscribe(it) }

}

fun <T> Maybe<T>.subscribeWith(subs: Subscriber): Disposable = with(subs).subscribe()

inline fun <T> Maybe<T>.shadowError(crossinline mapper: (Throwable) -> ShadowedError): Maybe<T> {
    return onErrorResumeNext { it: Throwable ->
        Maybe.error(mapper(it))
    }
}

inline fun <T> Maybe<T>.shadowErrorIfNot(crossinline mapper: (Throwable) -> ShadowedError): Maybe<T> {
    return onErrorResumeNext { it: Throwable ->
        if (it is ShadowedError) Maybe.error(it)
        else Maybe.error(mapper(it))
    }
}

//endregion

//region// Single

fun <T> Single<T>.with(subscriber: Subscriber) : SingleSubscribtioning<T> =
        SingleSubscribtioning(this, subscriber)

class SingleSubscribtioning<out T>(private val source: Single<T>,
                                   private val subscriber: Subscriber) {

    fun subscribe(onError: ErrorHandler? = null, onSuccess: ResultHandler<T>? = null) : Disposable =
            subscriber.subscribe(onSuccess, onError, null) { source.subscribe(it) }

}

fun <T> Single<T>.subscribeWith(subs: Subscriber): Disposable = with(subs).subscribe()

inline fun <T> Single<T>.shadowError(crossinline mapper: (Throwable) -> ShadowedError): Single<T> {
    return onErrorResumeNext { it: Throwable ->
        Single.error(mapper(it))
    }
}

inline fun <T> Single<T>.shadowErrorIfNot(crossinline mapper: (Throwable) -> ShadowedError): Single<T> {
    return onErrorResumeNext { it: Throwable ->
        if (it is ShadowedError) Single.error(it)
        else Single.error(mapper(it))
    }
}

//endregion

//region// Observable

fun <T> Observable<T>.with(subscriber: Subscriber): ObservableSubscribtioning<T> =
        ObservableSubscribtioning(this, subscriber)

class ObservableSubscribtioning<out T>(private val source: Observable<T>,
                                       private val subscriber: Subscriber) {

    fun subscribe(onError: ErrorHandler? = null, onComplete: CompleteHandler? = null,
                  onNext: ResultHandler<T>? = null) : Disposable =
            subscriber.subscribe(onNext, onError, onComplete) { source.subscribe(it) }

}

fun <T> Observable<T>.subscribeWith(subs: Subscriber): Disposable = with(subs).subscribe()

inline fun <T> Observable<T>.shadowError(crossinline mapper: (Throwable) -> ShadowedError): Observable<T> {
    return onErrorResumeNext { it: Throwable ->
        Observable.error(mapper(it))
    }
}

inline fun <T> Observable<T>.shadowErrorIfNot(crossinline mapper: (Throwable) -> ShadowedError): Observable<T> {
    return onErrorResumeNext { it: Throwable ->
        if (it is ShadowedError) Observable.error(it)
        else Observable.error(mapper(it))
    }
}

//endregion


private class ReDisposable: Disposable {

    var disposable: Disposable? = null

    override fun dispose() {
        disposable?.dispose()
    }

    override fun isDisposed(): Boolean = disposable?.isDisposed ?: true

}

