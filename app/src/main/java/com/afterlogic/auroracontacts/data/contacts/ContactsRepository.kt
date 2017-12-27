package com.afterlogic.auroracontacts.data.contacts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.afterlogic.auroracontacts.BuildConfig
import com.afterlogic.auroracontacts.R
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.application.AppScope
import com.afterlogic.auroracontacts.application.wrappers.Resources
import com.afterlogic.auroracontacts.data.db.ContactsDao
import com.afterlogic.auroracontacts.data.preferences.Prefs
import com.afterlogic.auroracontacts.data.util.RemoteServiceProvider
import io.reactivex.*
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.security.InvalidParameterException
import javax.inject.Inject

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

@AppScope
class ContactsRepository @Inject constructor(
        private val prefs: Prefs,
        private val dao: ContactsDao,
        private val remoteServiceProvider: RemoteServiceProvider<ContactsRemoteService>,
        private val mapper: ContactsMapper,
        private val res: Resources,
        private val crossProcessChangedPublisher: CrossProcessContactsDBChangedPublisher
) {

    companion object {
        private const val INNER_GROUP_ALL = -4L
        private const val INNER_GROUP_PERSONAL = -3L
        private const val INNER_GROUP_SHARED = -2L
        private const val INNER_GROUP_TEAM = -1L
    }

    private object ChangedEvent

    private val remoteService get() = remoteServiceProvider.get()

    private val innerChangedSubject = BehaviorSubject.createDefault(ChangedEvent)

    private val inTransactionSubject = BehaviorSubject.createDefault(false)

    private val innerContactsGroups: Flowable<List<ContactGroupInfo>> get() {
        return innerChangedSubject.map {

            val strings = res.strings

            listOf(
                    ContactGroupInfo(strings[R.string.value_contacts_all], INNER_GROUP_ALL, prefs.syncAllContacts),
                    ContactGroupInfo(strings[R.string.value_contacts_personal], INNER_GROUP_PERSONAL, prefs.syncPersonalContacts),
                    ContactGroupInfo(strings[R.string.value_contacts_shared], INNER_GROUP_SHARED, prefs.syncSharedContacts),
                    ContactGroupInfo(strings[R.string.value_contacts_team], INNER_GROUP_TEAM, prefs.syncTeamContacts)
            )

        }.toFlowable(BackpressureStrategy.LATEST)
    }

    private val allItemsSyncEnabled: Boolean get() {
        return prefs.syncTeamContacts && prefs.syncSharedContacts && prefs.syncPersonalContacts &&
                dao.syncDisabledCount == 0
    }

    fun getContactsGroupsInfo(): Flowable<List<ContactGroupInfo>> {

        return crossProcessChangedPublisher.listen()
                .startWith(CrossProcessContactsDBChangedPublisher.ContactsChangedEvent)
                .flatMap { dao.all }
                .map { it.map(mapper::toPlain) }
                .filter { prefs.contactsFetched }
                .combineLatest(innerContactsGroups)
                .combineLatest(inTransactionSubject.toFlowable(BackpressureStrategy.LATEST))
                .filter { (_, inTransaction) -> !inTransaction }
                .map { (data, _) -> data }
                .map { (fromDao, inner) -> inner + fromDao }
                .mergeWith(loadRemote().toCompletable().toFlowable())

    }

    fun setSyncEnabled(contactsGroup: ContactGroupInfo, enabled: Boolean) : Completable {
        return Completable.fromAction {

            inTransactionSubject.onNext(true)

            if (contactsGroup.id < 0) {

                when(contactsGroup.id) {
                    INNER_GROUP_ALL -> {
                        prefs.syncPersonalContacts = enabled
                        prefs.syncSharedContacts = enabled
                        prefs.syncTeamContacts = enabled
                        dao.setAllSyncEnabled(enabled)
                    }
                    INNER_GROUP_PERSONAL -> prefs.syncPersonalContacts = enabled
                    INNER_GROUP_SHARED -> prefs.syncSharedContacts = enabled
                    INNER_GROUP_TEAM -> prefs.syncTeamContacts = enabled
                    else -> throw InvalidParameterException("Unknown inner group.")
                }

                innerChangedSubject.onNext(ChangedEvent)

            } else {

                dao.setSyncEnabled(contactsGroup.id, enabled)

            }

            val allSyncEnabled = allItemsSyncEnabled
            if (prefs.syncAllContacts != allSyncEnabled) {

                prefs.syncAllContacts = allSyncEnabled
                innerChangedSubject.onNext(ChangedEvent)

            }

            inTransactionSubject.onNext(false)

        }
    }

    fun loadRemote(): Single<List<RemoteContactGroup>> {

        return remoteService.flatMap { it.getContactsGroups() }
                .doOnSuccess {

                    val dbeItems = it.map { mapper.toDbe(it, prefs.syncAllContacts) }
                    dao += dbeItems
                    prefs.contactsFetched = true

                    crossProcessChangedPublisher.onChange()

                }

    }

    class CrossProcessContactsDBChangedPublisher @Inject constructor(
            private val context: App
    ) {

        companion object {
            private const val ACTION = "CrossProcessContactsDBChangedPublisher.CHANGED"
            private const val PID = "CrossProcessContactsDBChangedPublisher.PID"
        }

        object ContactsChangedEvent

        val pid = android.os.Process.myPid()

        fun onChange() {
            val intent = Intent(ACTION)
                    .putExtra(PID, pid)
                    .setPackage(BuildConfig.APPLICATION_ID)
            context.sendBroadcast(intent)
        }

        fun listen(): Flowable<ContactsChangedEvent> {

            val finalizers = arrayListOf<() -> Unit>()

            val source = { emitter: FlowableEmitter<ContactsChangedEvent> ->

                val receiver = object : BroadcastReceiver() {

                    override fun onReceive(ctx: Context, intent: Intent) {

                        if (intent.action != ACTION) return

                        if (intent.getIntExtra(PID, -1) != pid) {
                            Timber.d("Receive cross process contacts db changed event.")
                            emitter.onNext(ContactsChangedEvent)
                        }

                    }

                }

                context.registerReceiver(
                        receiver,
                        IntentFilter(ACTION)
                )

                finalizers.add { context.unregisterReceiver(receiver) }.let { Unit }

            }

            return Flowable.create<ContactsChangedEvent>(source, BackpressureStrategy.LATEST)
                    .doFinally { finalizers.forEach { it() } }

        }

    }

}