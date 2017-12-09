package com.afterlogic.auroracontacts.data.db

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Entity(tableName = "calendar")
data class CalendarDbe(
        @PrimaryKey
        val id: String,
        val name: String,
        val description: String?,
        val color: Int,
        @field:Embedded(prefix = "sync_settings_")
        val settings: SyncSettings
)

data class SyncSettings (
        val syncEnabled: Boolean
)