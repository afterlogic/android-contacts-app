package com.afterlogic.auroracontacts.data.contacts

import com.afterlogic.auroracontacts.data.db.ContactGroupDbe
import javax.inject.Inject

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

class ContactsMapper @Inject constructor() {

    fun toDbe(remote: RemoteContactGroup, syncEnabled: Boolean = false): ContactGroupDbe {
        return ContactGroupDbe(remote.id, remote.name, syncEnabled)
    }

    fun toPlain(dbe: ContactGroupDbe): ContactGroupInfo {
        return ContactGroupInfo(dbe.name, dbe.id, dbe.syncEnabled)
    }

}