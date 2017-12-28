package com.afterlogic.auroracontacts.presentation.background.sync

import android.provider.ContactsContract

/**
 * Created by sunny on 11.12.2017.
 * mail: mail@sunnydaydev.me
 */

object CustomContact {

    object Calendar {
        const val REMOTE_ID = "cal_sync1"
        const val REMOTE_CTAG = "cal_sync2"
    }


    object Events {
        const val REMOTE_ID = "sync_data1"
        const val REQURENCE_ID = "sync_data2"
        const val REMOTE_ETAG = "sync_data3"
        const val SYNCED = "sync_data4"

    }

    object Contacts {
        const val REMOTE_ID = ContactsContract.Contacts.Data.SYNC1
        const val SYNCED = ContactsContract.Contacts.Data.SYNC2
        const val ETAG = ContactsContract.Contacts.Data.SYNC3
    }

}
