package com.afterlogic.auroracontacts.core.rx

import com.afterlogic.auroracontacts.BuildConfig
import com.afterlogic.auroracontacts.core.contextWrappers.Toaster
import com.afterlogic.auroracontacts.core.util.isAnyNetworkError
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by aleksandrcikin on 25.04.17.
 *
 */
@Singleton
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
                .dropWhile { !it.className.startsWith(classPackage) }
                .dropWhile { it.className.startsWith(classPackage) }
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

        val cause = e.cause

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

typealias ErrorHandler = (Throwable) -> Boolean
typealias ResultHandler<T> = (T) -> Unit
typealias CompleteHandler = () -> Unit

class SuccessErrorHandler(private val action: (Throwable) -> Unit): ErrorHandler {

    override fun invoke(p1: Throwable): Boolean {
        action(p1)
        return true
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


//endregion


private class ReDisposable: Disposable {

    var disposable: Disposable? = null

    override fun dispose() {
        disposable?.dispose()
    }

    override fun isDisposed(): Boolean = disposable?.isDisposed ?: true

}

