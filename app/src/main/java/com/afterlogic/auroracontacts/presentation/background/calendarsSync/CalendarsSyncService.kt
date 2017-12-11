package com.afterlogic.auroracontacts.presentation.background.calendarsSync

import android.accounts.Account
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.CalendarContract
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.data.calendar.CalendarMapper
import com.afterlogic.auroracontacts.data.calendar.CalendarRemoteServiceProvider
import com.afterlogic.auroracontacts.data.calendar.RemoteCalendar
import com.afterlogic.auroracontacts.data.db.CalendarsDao
import com.afterlogic.auroracontacts.presentation.background.syncStateService.SyncStateHolder
import com.afterlogic.auroracontacts.presentation.common.base.InjectionDaggerService
import io.reactivex.Completable
import io.reactivex.rxkotlin.Singles
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

class CalendarsSyncService: InjectionDaggerService<CalendarsSyncInjection>() {

    private val syncAdapter by inject { it.adapter }
    private val stateHolder by inject { it.syncStateHolder }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        stateHolder.calendarsSyncing.onNext(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        stateHolder.calendarsSyncing.onNext(false)
    }

    override fun onBind(intent: Intent?): IBinder = syncAdapter.syncAdapterBinder

}

class CalendarsSyncInjection @Inject constructor(
        val adapter: CalendarSyncAdapter,
        val syncStateHolder: SyncStateHolder
)

@AppScope
class CalendarSyncAdapter @Inject constructor(
        context: App,
        private val calendarSyncOperation: CalendarSyncOperation.Factory
): AbstractThreadedSyncAdapter(context, true, false) {

    override fun onPerformSync(account: Account,
                               params: Bundle?,
                               authority: String,
                               contentProviderClient: ContentProviderClient,
                               syncResult: SyncResult?) {

        Timber.d("onPerformSync")

        calendarSyncOperation.create(account, contentProviderClient)
                .sync()
                .blockingGet()
                ?.let(Timber::e)

        Timber.d("onPerformSyncEnd")

    }



}

class CalendarSyncOperation(private val account: Account,
                            private val contentClient: ContentProviderClient,
                            private val dao: CalendarsDao,
                            private val calendarMapper: CalendarMapper,
                            private val remoteServiceProvider: CalendarRemoteServiceProvider ) {

    class Factory @Inject constructor(
            private val dao: CalendarsDao,
            private val calendarMapper: CalendarMapper,
            private val remoteServiceProvider: CalendarRemoteServiceProvider
    ) {

        fun create(account: Account, client: ContentProviderClient) : CalendarSyncOperation {
            return CalendarSyncOperation(account, client, dao, calendarMapper, remoteServiceProvider)
        }

    }

    fun sync() : Completable = Completable.defer {

        val remoteCalendars = remoteServiceProvider.get().flatMap { it.getCalendars() }

        val local = dao.all.map { it.map { calendarMapper.toPlain(it) } }.firstOrError()

        Singles.zip(remoteCalendars, local) { rem, loc ->
            val syncable = loc.filter { it.settings.syncEnabled } .map { it.id }
            rem.filter { syncable.contains(it.id) }
        }
                .flatMapCompletable(this::sync)

    }

    fun sync(calendars: List<RemoteCalendar>): Completable = Completable.fromAction {

        val calendarUri = getSyncUri(CalendarContract.Calendars.CONTENT_URI)

        // Delete unsyncable calendars
        val queryIds = calendars.joinToString("', '", "('", "')") { it.id }
        contentClient.delete(calendarUri, "${Contract.Calendar.REMOTE_ID} NOT IN $queryIds", null)

        // Update and insert
        calendars
                .map { it.id to it.toContentValues() }
                // Try update exists
                .filter { (id, cv) ->
                    contentClient.update(
                            calendarUri, cv,
                            "${Contract.Calendar.REMOTE_ID} = ?", arrayOf(id)
                    ) == 0
                }
                // Insert new
                .forEach { (_, cv) ->
                    contentClient.insert(calendarUri, cv)
                }

    }


    private fun getSyncUri(uri: Uri): Uri {
        return uri.buildUpon()
                .appendQueryParameter("caller_is_syncadapter", "true")
                .appendQueryParameter("account_name", account.name)
                .appendQueryParameter("account_type", account.type)
                .build()
    }

    // TODO: Timezone
    private fun RemoteCalendar.toContentValues(
            localId: Long? = null,
            timeZone: TimeZone = TimeZone.getDefault()
    ): ContentValues {

        val cv = ContentValues()

        localId?.let { cv.put(CalendarContract.Calendars._ID, it) }

        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
        cv.put(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, this.name)
        cv.put(CalendarContract.Calendars.CALENDAR_COLOR, this.color)
        cv.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.id)
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        cv.put(CalendarContract.Calendars.DIRTY, 0)
        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, this.owner)
        cv.put(Contract.Calendar.REMOTE_ID, this.id)
        cv.put(Contract.Calendar.REMOTE_CTAG, this.cTag)
        cv.put(
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                if (this.accessLevel == RemoteCalendar.AccessLevel.EDITOR)
                    CalendarContract.Calendars.CAL_ACCESS_EDITOR
                else CalendarContract.Calendars.CAL_ACCESS_READ
        )
        cv.put(CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES, 0)
        cv.put(CalendarContract.Calendars.ALLOWED_AVAILABILITY, 0)

        return cv

    }

}