package com.afterlogic.auroracontacts.core.rx

import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.Subject

/**
 * Created by sunny on 23.10.2017.
 * mail: mail@sunnydaydev.me
 */

//region// Creations

fun <T> T?.toMaybe(): Maybe<T> {
    return this?.let { Maybe.just(it) } ?: Maybe.empty()
}

//endregion

inline fun <S, reified T> Single<S>.cast(): Single<T> = cast(T::class.java)


//region// Subjects

operator fun <T> Subject<T>.invoke(value: T) {
    onNext(value)
}

//endregion
