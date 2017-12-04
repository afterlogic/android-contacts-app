package com.afterlogic.auroracontacts.core.rx

import io.reactivex.*
import io.reactivex.functions.Function
import io.reactivex.subjects.Subject
import kotlin.reflect.KClass

/**
 * Created by sunny on 23.10.2017.
 * mail: mail@sunnydaydev.me
 */

//region// Creations

fun <T> T?.toMaybe(): Maybe<T> {
    return this?.let { Maybe.just(it) } ?: Maybe.empty()
}

//endregion

//region// Errors handling

fun Completable.onErrorsComplete(vararg errors: KClass<*>) : Completable {
    return onErrorResumeNext {
        if (it::class in errors) {
            Completable.complete()
        } else {
            Completable.error(it)
        }
    }
}

fun <T> Maybe<T>.onErrorsComplete(vararg errors: KClass<*>) : Maybe<T> =
        onErrorComplete { it::class in errors }

fun <T> Single<T>.onErrorsComplete(vararg errors: KClass<*>) : Maybe<T> =
        toMaybe().onErrorComplete { it::class in errors }

fun <T> Observable<T>.onErrorsComplete(vararg errors: KClass<*>) : Observable<T> {
    return onErrorResumeNext( Function {
        if (it::class in errors) {
            Observable.empty()
        } else {
            Observable.error(it)
        }
    })
}

//endregion

inline fun <T> Observable<T>.best(crossinline check: (current: T, next: T) -> Boolean): Maybe<T> {

    var best: T? = null

    return this
            .doOnNext { next ->  best = best?.takeIf { !check(it, next) } ?: next }
            .ignoreElements()
            .andThen(Maybe.defer { best.toMaybe() })

}

inline fun <T> Flowable<T>.best(crossinline check: (current: T, next: T) -> Boolean): Maybe<T> {

    var best: T? = null

    return this
            .doOnNext { next ->  best = best?.takeIf { !check(it, next) } ?: next }
            .ignoreElements()
            .andThen(Maybe.defer { best.toMaybe() })

}


//region// Subjects

operator fun <T> Subject<T>.invoke(value: T) {
    onNext(value)
}

//endregion
