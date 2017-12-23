package com.afterlogic.auroracontacts.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.afterlogic.auroracontacts.core.util.Optional
import com.afterlogic.auroracontacts.core.util.SimpleActivityLifecycleCallbacks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Created by sunny on 30.08.17.
 *
 */

@AppScope
class ActivityTracker @Inject
internal constructor(context: App) {

    enum class Level { CREATED, STARTED, RESUMED }

    val lastCreatedActivity: Activity?
        get() = created.lastOrNull()

    val lastStartedActivity: Activity?
        get() = started.lastOrNull()

    val lastResumedActivity: Activity?
        get() = resumed.lastOrNull()

    val isAppInForeground: Boolean
        get() = started.isNotEmpty()

    val isAppInForegroundRx: Observable<Boolean>
        get() = changedPublisher.filter { it == Level.STARTED }
                .map { lastStartedActivity != null }
                .startWith(lastStartedActivity != null)
                .distinctUntilChanged()

    val lastResumedActivityRx: Observable<Optional<Activity>> get() {

        val startedOptional: () -> Optional<Activity> = {
            lastStartedActivity?.let { Optional(it) } ?: Optional()
        }

        return changedPublisher
                .map { startedOptional() }
                .startWith(Observable.fromCallable { startedOptional() })

    }

    private val application: Application = context.applicationContext as Application

    private val created = mutableSetOf<Activity>()
    private val started = mutableSetOf<Activity>()
    private val resumed = mutableSetOf<Activity>()

    private val changedPublisher = PublishSubject.create<Level>()

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks by SimpleActivityLifecycleCallbacks() {

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
            created.add(activity)
            changedPublisher.onNext(Level.CREATED)
        }

        override fun onActivityStarted(activity: Activity) {
            started.add(activity)
            changedPublisher.onNext(Level.STARTED)
        }

        override fun onActivityResumed(activity: Activity) {
            resumed.add(activity)
            changedPublisher.onNext(Level.RESUMED)
        }

        override fun onActivityPaused(activity: Activity) {
            resumed.remove(activity)
            changedPublisher.onNext(Level.RESUMED)
        }

        override fun onActivityStopped(activity: Activity) {
            started.remove(activity)
            changedPublisher.onNext(Level.STARTED)
        }

        override fun onActivityDestroyed(activity: Activity) {
            created.remove(activity)
            changedPublisher.onNext(Level.CREATED)
        }

    }

    fun register() {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

}
