package com.afterlogic.auroracontacts.presentation.background.sync

import android.provider.CalendarContract
import android.provider.ContactsContract

/**
 * Created by sunny on 11.12.2017.
 * mail: mail@sunnydaydev.me
 */

private typealias _Calendar = CalendarContract.Calendars
private typealias _Events = CalendarContract.Events
private typealias _RawContacts = ContactsContract.RawContacts

object CustomContact {

    object Calendar {
        const val REMOTE_ID = _Calendar.CAL_SYNC1
        const val REMOTE_CTAG = _Calendar.CAL_SYNC2
    }


    object Events {
        const val REMOTE_ID = _Events.SYNC_DATA1
        const val REQURENCE_ID = _Events.SYNC_DATA2
        const val REMOTE_ETAG = _Events.SYNC_DATA3
        const val SYNCED = _Events.SYNC_DATA4

    }

    object RawContacts {
        const val REMOTE_ID = _RawContacts.SYNC1
        const val SYNCED = _RawContacts.SYNC2
        const val ETAG = _RawContacts.SYNC3
        const val SYNC_CYCLE_ID = _RawContacts.SYNC4
    }

}
