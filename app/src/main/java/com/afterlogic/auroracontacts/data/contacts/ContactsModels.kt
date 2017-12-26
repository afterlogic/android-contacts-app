package com.afterlogic.auroracontacts.data.contacts

/**
 * Created by sunny on 26.12.2017.
 * mail: mail@sunnydaydev.me
 */

data class RemoteContactGroup(
        val name: String,
        val id: Long
)

data class ContactGroupInfo(
        val name: String,
        val id: Long,
        val syncing: Boolean
)