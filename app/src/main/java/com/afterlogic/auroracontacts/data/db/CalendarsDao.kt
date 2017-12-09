package com.afterlogic.auroracontacts.data.db

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Dao
interface CalendarsDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun store(calendars: List<CalendarDbe>)

    @get:Transaction
    @get:Query("SELECT * FROM `calendar`")
    val all: Flowable<List<CalendarDbe>>

    @Delete
    fun delete(calendar: CalendarDbe)

    @Transaction
    @Query("""
        UPDATE `calendar`
        SET `sync_settings_syncEnabled` = :enabled
        WHERE `id` = :id
    """)
    fun setSyncEnabled(id: String, enabled: Boolean)

}

operator fun CalendarsDao.plusAssign(calendars: List<CalendarDbe>) {
    store(calendars)
}

operator fun CalendarsDao.minusAssign(calendars: CalendarDbe) {
    delete(calendars)
}