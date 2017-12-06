package com.afterlogic.auroracontacts.data.account

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.afterlogic.auroracontacts.core.util.Optional
import com.afterlogic.auroracontacts.core.util.toOptional
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by sunny on 06.12.2017.
 * mail: mail@sunnydaydev.me
 */
internal class CurrentAccountSource
constructor(private val accountManager: AccountManager) : ObservableSource<Optional<Account>> {

    private val handler = Handler(Looper.getMainLooper())

    private val observers = mutableSetOf<Observer<in Optional<Account>>>()

    private val started = AtomicBoolean(false)

    private var lastResult: Optional<Account>? = null

    private val listener = OnAccountsUpdateListener {

       it.firstOrNull { it.type == AccountService.ACCOUNT_TYPE }
               .toOptional()
               .also { lastResult = it }
               .let { account -> observers.forEach { it.onNext(account) } }

    }

    override fun subscribe(observer: Observer<in Optional<Account>>) {

        observers.add(observer)

        observer.onSubscribe(object : Disposable {

            override fun isDisposed(): Boolean = !observers.contains(observer)

            override fun dispose() {
                observers.remove(observer)
                checkState()
            }

        })

        checkState()

        lastResult?.takeIf { observers.contains(observer) } ?.let { observer.onNext(it) }

    }

    private fun checkState() {

        synchronized(observers) {

            if (observers.isNotEmpty()) start()
            else stop()

        }

    }

    private fun start() {

        if (started.getAndSet(true)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            accountManager.addOnAccountsUpdatedListener(
                    listener, handler, true, arrayOf(AccountService.ACCOUNT_TYPE)
            )

        } else {

            accountManager.addOnAccountsUpdatedListener(listener, handler, true)

        }

    }

    private fun stop() {

        if (!started.getAndSet(false)) return

        accountManager.removeOnAccountsUpdatedListener(listener)

        lastResult = null

    }

}