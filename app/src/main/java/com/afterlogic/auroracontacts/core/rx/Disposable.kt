package com.afterlogic.auroracontacts.core.rx

import com.afterlogic.auroracontacts.core.util.Optional
import com.afterlogic.auroracontacts.core.util.compareAndSet
import io.reactivex.*
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by sashka on 02.03.17.
 *
 *
 * mail: sunnyday.development@gmail.com
 */
class DisposableBag {

    private val disposables = HashSet<Disposable>()
    private val bags = HashSet<DisposableBag>()

    constructor()

    constructor(vararg bags: DisposableBag) : super() {
        bags.forEach { addBag(it) }
    }

    fun add(disposable: Disposable) {
        disposables.add(disposable)
    }

    fun addBag(bag: DisposableBag) {
        bags.add(bag)
    }

    fun remove(disposable: Disposable) {
        disposables.remove(disposable)
    }

    fun removeBag(disposableBag: DisposableBag) {
        bags.remove(disposableBag)
    }

    @Synchronized
    fun dispose() {

        disposables.forEach { it.dispose() }
        disposables.clear()

        bags.forEach { it.dispose() }

    }

    fun <T> track(upstream: Observable<T>): Observable<T> {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { disposable ->
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) }  }
    }

    fun track(upstream: Completable): Completable {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { disposable ->
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) } }
    }

    fun <T> track(upstream: Single<T>): Single<T> {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { disposable ->
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) } }
    }

    fun <T> track(upstream: Maybe<T>): Maybe<T> {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe { disposable ->
                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) } }
    }

    fun <T> track(upstream: Flowable<T>): Flowable<T> {
        val disposableHolder = AtomicReference<Disposable?>(null)
        return upstream
                .doOnSubscribe {

                    val disposable = object : Disposable {

                        private val disposed = AtomicBoolean(false)

                        override fun isDisposed(): Boolean = disposed.get()

                        override fun dispose() {
                            disposed.compareAndSet(true) {
                                it.cancel()
                            }
                        }

                    }

                    disposableHolder.set(disposable)
                    add(disposable)
                }
                .doFinally { disposableHolder.get()?.let { remove(it) } }
    }

}

fun Completable.disposeBy(bag: DisposableBag): Completable = compose { bag.track(it) }

fun <T> Maybe<T>.disposeBy(bag: DisposableBag): Maybe<T> = compose { bag.track(it) }

fun <T> Single<T>.disposeBy(bag: DisposableBag): Single<T> = compose { bag.track(it) }

fun <T> Observable<T>.disposeBy(bag: DisposableBag): Observable<T> = compose { bag.track(it) }

fun <T> Flowable<T>.disposeBy(bag: DisposableBag): Flowable<T> = compose { bag.track(it) }


class OptionalDisposable : Optional<Disposable>() {

    fun track(upstream: Completable): Completable {
        return upstream.doOnSubscribe { this.set(it) }
                .doFinally { this.set(null) }
    }

    fun <T> track(upstream: Maybe<T>): Maybe<T> {
        return upstream.doOnSubscribe { this.set(it) }
                .doFinally { this.set(null) }
    }

    fun <T> track(upstream: Single<T>): Single<T> {
        return upstream.doOnSubscribe { this.set(it) }
                .doFinally { this.set(null) }
    }

    fun <T> track(upstream: Observable<T>): Observable<T> {
        return upstream.doOnSubscribe { this.set(it) }
                .doFinally { this.set(null) }
    }

    fun <T> track(upstream: Flowable<T>): Flowable<T> {
        return upstream.doOnSubscribe { subscription -> set(SubscriptionDisposable(subscription)) }
                .doFinally { this.set(null) }
    }

    @Synchronized
    fun disposeAndClear() {
        get()?.dispose()
        set(null)
    }

}

fun Completable.disposeBy(bag: OptionalDisposable): Completable = compose { bag.track(it) }

fun <T> Maybe<T>.disposeBy(bag: OptionalDisposable): Maybe<T> = compose { bag.track(it) }

fun <T> Single<T>.disposeBy(bag: OptionalDisposable): Single<T> = compose { bag.track(it) }

fun <T> Observable<T>.disposeBy(bag: OptionalDisposable): Observable<T> = compose { bag.track(it) }



private class SubscriptionDisposable(private val subscription: Subscription) : Disposable {

    private var cancelled = false

    override fun dispose() {

        if (cancelled) return
        cancelled = true

        subscription.cancel()
    }

    override fun isDisposed(): Boolean = cancelled

}
