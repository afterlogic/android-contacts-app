package com.afterlogic.auroracontacts.data.auth

import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.core.rx.toMaybe
import com.afterlogic.auroracontacts.data.AuthFailedError
import com.afterlogic.auroracontacts.data.account.AccountService
import com.afterlogic.auroracontacts.data.api.UserNotAuthorizedError
import com.afterlogic.auroracontacts.data.auth.model.AuthorizedAuroraSession
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * Created by sunny on 13.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class AuthResolver @Inject constructor(
        private val accountService: AccountService,
        private val authenticatorService: AuthenticatorService
) {

    private object ReLoggedIn

    private var weakRelogin: WeakReference<Flowable<ReLoggedIn>>? = null

    val checkAndResolveAuth: Function<Flowable<Throwable>, Publisher<*>> get() {

        return Function {

            val firstCheck = AtomicReference(true)

            it.flatMap { e: Throwable ->

                val firstTime = firstCheck.getAndSet(false)
                val needRelogin = firstTime && e is AuthFailedError && !e.checked

                val handleError = if (needRelogin) {
                    relogin()
                } else {
                    Flowable.error(e)
                }

                handleError
                        .onErrorResumeNext { e: Throwable ->
                            (e as? AuthFailedError)?.markChecked()
                            Flowable.error(e)
                        }

            }

        }

    }

    private fun relogin(): Flowable<ReLoggedIn> {

        return weakRelogin?.get() ?: accountService.accountSession
                .firstOrError()
                .flatMapMaybe { it.get().toMaybe() }
                .observeOn(Schedulers.io())
                .flatMapSingle<AuthorizedAuroraSession> map@ {
                    val password = it.password ?: return@map Single.error(UserNotAuthorizedError())
                    val email = it.email ?: return@map Single.error(UserNotAuthorizedError())
                    authenticatorService.login(it.domain, email, password)
                }
                .flatMapCompletable { accountService.updateCurrentAccount(it) }
                .andThen(Flowable.just(ReLoggedIn))
                .share()
                .doFinally { weakRelogin = null }
                .also { weakRelogin = WeakReference(it) }

    }

}