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

object CustomContract {

    object Calendar {
        const val REMOTE_ID = _Calendar.CAL_SYNC1
        const val REMOTE_CTAG = _Calendar.CAL_SYNC2
    }

    object Events {
        const val REMOTE_ID = _Events.SYNC_DATA1
        const val REQURENCE_ID = _Events.SYNC_DATA2
        const val REMOTE_ETAG = _Events.SYNC_DATA3
        const val SYNCED = _Events.SYNC_DATA4

        const val ATTENDEES_ETAG = _Events.SYNC_DATA5
        const val ALARMS_ETAG = _Events.SYNC_DATA6

        object Attendees {

            const val PARITCIPATION_STATUS = CalendarContract.Attendees.SYNC_DATA1
            const val ROLE = CalendarContract.Attendees.SYNC_DATA2
            const val PARITCIPATION_LEVEL = CalendarContract.Attendees.SYNC_DATA3

        }

    }

    object Contacts {

        const val REMOTE_ID = _RawContacts.SYNC1
        const val SYNCED = _RawContacts.SYNC2
        const val ETAG = _RawContacts.SYNC3


        object Groups {

            const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.afterlogic.aurora.contacts.GroupsIds"

            const val GROUPS = ContactsContract.Data.DATA1

        }

        object Birthday {

            const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.afterlogic.aurora.contacts.Birthday"

            const val DAY = ContactsContract.Data.DATA1
            const val MONTH = ContactsContract.Data.DATA2
            const val YEAR = ContactsContract.Data.DATA3

        }

    }

}
