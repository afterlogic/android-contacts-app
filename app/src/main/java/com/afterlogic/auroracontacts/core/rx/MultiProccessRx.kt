package com.afterlogic.auroracontacts.core.rx

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import com.afterlogic.auroracontacts.core.util.compareAndSet
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by sunny on 10.12.2017.
 * mail: mail@sunnydaydev.me
 */

private const val OBSERVABLE_MESSAGE_SUBSCRIBE = 3
private const val OBSERVABLE_MESSAGE_ON_SUBSCRIBE = 2
private const val OBSERVABLE_MESSAGE_ON_NEXT = 4
private const val OBSERVABLE_MESSAGE_ON_COMPLETE = 5
private const val OBSERVABLE_MESSAGE_ON_ERROR = 6
private const val OBSERVABLE_MESSAGE_DISPOSE = 7

open class ObservableServiceError(message: String): Throwable(message)

class ServiceConnectionError: ObservableServiceError("Error happen on service side.")
class ServiceDisconnectedError : ObservableServiceError("Service was disconnected before observable was terminated.")
class ServiceNotBoundedError: ObservableServiceError("Service not started.")

private class MessageHandler(
        looper: Looper,
        private val messageHandler: (Message) -> Unit
): android.os.Handler(looper) {

    val messenger = Messenger(this)

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)

        messageHandler(msg)

    }

}

abstract class ObservableMessenger(looper: Looper = Looper.getMainLooper()) {

    private val connections = mutableMapOf<Int, Disposable>()

    private val messageHandler = MessageHandler(looper) {

        when(it.what) {

            OBSERVABLE_MESSAGE_SUBSCRIBE -> {

                val requestCode = it.arg1
                val id = it.arg2

                Timber.d("SUBSCRIBE: $id: requestCode: $requestCode")

                val observableMessenger = it.obj as Messenger

                getObservable(requestCode).subscribeWith(Observer(id, observableMessenger))

            }

            OBSERVABLE_MESSAGE_DISPOSE -> {

                val id = it.arg1

                Timber.d("DISPOSE: $id")

                connections[id]?.dispose()

            }

        }

    }

    private val messenger = Messenger(messageHandler)

    val binder: IBinder = messenger.binder

    abstract fun getObservable(requestCode: Int): Observable<Bundle>

    private inner class Observer(
            private val id: Int,
            private val observableMessenger: Messenger
    ): io.reactivex.Observer<Bundle> {

        override fun onNext(data: Bundle) {
            observableMessenger.send {
                what = OBSERVABLE_MESSAGE_ON_NEXT
                this.data = data
            }
        }

        override fun onError(e: Throwable) {
            observableMessenger.send {
                what = OBSERVABLE_MESSAGE_ON_ERROR
            }
        }

        override fun onSubscribe(d: Disposable) {
            connections[id] = d
        }

        override fun onComplete() {
            observableMessenger.send {
                what = OBSERVABLE_MESSAGE_ON_COMPLETE
            }
        }

    }

}

class MultiProcessObservable<T>(
        private val context: Context,
        private val intent: Intent,
        private val requestCode: Int,
        private val autoCreate:Boolean = false,
        private val looper: Looper = Looper.getMainLooper(),
        private val mapper: (Bundle) -> T
) : Observable<T>() {

    override fun subscribeActual(observer: Observer<in T>) {

        val finalizers = mutableListOf<() -> Unit>()

        Observable.create<Bundle> { emitter ->

            val connection = ObservableServiceConnection(requestCode, emitter, looper)

            finalizers.add {
                connection.dispose()
                context.unbindService(connection)
            }

            val flags = if (autoCreate) Context.BIND_AUTO_CREATE else 0

            if (!context.bindService(intent, connection, flags)) {

                emitter.onError(ServiceNotBoundedError())

            }

        }
                .doOnError(Timber::e)
                .map(mapper)
                .doFinally { finalizers.forEach { it() } }
                .subscribeWith(observer)

    }

}

private class ObservableServiceConnection(
        private val requestCode: Int,
        private val emitter: ObservableEmitter<Bundle>,
        looper: Looper
): ServiceConnection {

    companion object {

        private var lastConnectionId = 0

    }

    private var serviceMessenger: Messenger? = null
    private var id = ++lastConnectionId

    private val disposed = AtomicBoolean(false)

    private val observableHandler = MessageHandler(looper) {

        when(it.what) {

            OBSERVABLE_MESSAGE_ON_SUBSCRIBE -> {
                id = it.arg1
                Timber.d("ON_SUBSCRIBE: $id")
            }

            OBSERVABLE_MESSAGE_ON_NEXT -> {
                Timber.d("ON_NEXT: $id")
                emitter.onNext(it.data ?: Bundle.EMPTY)
            }

            OBSERVABLE_MESSAGE_ON_COMPLETE -> {
                Timber.d("ON_COMPLETE: $id")
                emitter.onComplete()
            }

            OBSERVABLE_MESSAGE_ON_ERROR -> {
                Timber.d("ON_ERROR: $id")
                emitter.tryOnError(ServiceConnectionError())
            }

        }

    }

    override fun onServiceDisconnected(componentName: ComponentName) {

        Timber.d("onServiceDisconnected: $id")

        if (!disposed.get()) emitter.onError(ServiceDisconnectedError())
        else emitter.onComplete()

    }

    override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {

        Timber.d("onServiceConnected: $id")

        if (disposed.get()) return

        serviceMessenger = Messenger(binder)

        serviceMessenger?.send {
            what = OBSERVABLE_MESSAGE_SUBSCRIBE
            arg1 = requestCode
            arg2 = id
            obj = observableHandler.messenger
        }

    }

    fun dispose() {

        if (!disposed.compareAndSet(true)) return

        serviceMessenger?.send {
            what = OBSERVABLE_MESSAGE_DISPOSE
            arg1 = id
        }

    }

}

private fun Messenger.send(applier: (Message).() -> Unit) {
    Message.obtain().apply(applier).let { send(it) }
}