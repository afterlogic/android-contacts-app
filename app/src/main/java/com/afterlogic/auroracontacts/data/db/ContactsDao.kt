package com.afterlogic.auroracontacts.data.db

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */

@Dao
abstract class ContactsDao {

    //region// Contact's insertion

    @get:Query("""
        SELECT `id` FROM `contact_groups`
        WHERE `syncEnabled` = 1
    """)
    protected abstract val syncEnabledIds: List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(calendars: List<ContactGroupDbe>)

    @Transaction
    open fun store(calendars: List<ContactGroupDbe>) {

        val syncEnabledIds = syncEnabledIds

        deleteAll()

        insert(calendars)

        setSyncEnabled(syncEnabledIds)

    }

    operator fun plusAssign(calendars: List<ContactGroupDbe>) { store(calendars) }

    //endregion

    //region// Contact's updates

    @Transaction
    @Query("""
        UPDATE `contact_groups`
        SET `syncEnabled` = :enabled
        WHERE `id` = :id
    """)
    abstract fun setSyncEnabled(id: Long, enabled: Boolean)

    @Transaction
    @Query("""
        UPDATE `contact_groups`
        SET `syncEnabled` = 1
        WHERE `id` IN (:ids)
    """)
    protected abstract fun setSyncEnabled(ids: List<Long>)

    @Transaction
    @Query("""
        UPDATE `contact_groups`
        SET `syncEnabled` = :enabled
    """)
    abstract fun setAllSyncEnabled(enabled: Boolean)

    @get:Transaction
    @get:Query("""
        SELECT COUNT(*) FROM `contact_groups`
        WHERE `syncEnabled` = 0
    """)
    abstract val syncDisabledCount: Int

    //endregion

    //region// Contact's getters

    @get:Transaction
    @get:Query("SELECT * FROM `contact_groups`")
    abstract val all: Flowable<List<ContactGroupDbe>>

    //endregion

    //region// Calendar's deleting

    @Delete
    abstract fun delete(calendar: ContactGroupDbe)

    @Query("DELETE FROM `contact_groups`")
    abstract fun deleteAll()

    operator fun minusAssign(calendars: ContactGroupDbe) { delete(calendars) }

    //endregion
}