package com.afterlogic.auroracontacts.data.db

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Dao
abstract class CalendarsDao {

    //region// Calendar's insertion

    @get:Query("""
        SELECT `id` FROM `calendar`
        WHERE `sync_settings_syncEnabled` = 1
    """)
    abstract protected val syncEnabledIds: List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract protected fun insert(calendars: List<CalendarDbe>)

    @Transaction
    open fun store(calendars: List<CalendarDbe>) {

        val syncEnabledIds = syncEnabledIds

        deleteAll()

        insert(calendars)

        setSyncEnabled(syncEnabledIds)

    }

    operator fun plusAssign(calendars: List<CalendarDbe>) { store(calendars) }

    //endregion

    //region// Calendar's updates

    @Transaction
    @Query("""
        UPDATE `calendar`
        SET `sync_settings_syncEnabled` = :enabled
        WHERE `id` = :id
    """)
    abstract fun setSyncEnabled(id: String, enabled: Boolean)

    @Transaction
    @Query("""
        UPDATE `calendar`
        SET `sync_settings_syncEnabled` = 1
        WHERE `id` IN (:ids)
    """)
    abstract fun setSyncEnabled(ids: List<String>)

    //endregion

    //region// Calendar's getters

    @get:Transaction
    @get:Query("SELECT * FROM `calendar`")
    abstract val all: Flowable<List<CalendarDbe>>

    //endregion

    //region// Calendar's deleting

    @Delete
    abstract fun delete(calendar: CalendarDbe)

    @Query("DELETE FROM `calendar`")
    abstract fun deleteAll()

    operator fun minusAssign(calendars: CalendarDbe) { delete(calendars) }

    //endregion

}