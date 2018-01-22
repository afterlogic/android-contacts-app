package com.afterlogic.auroracontacts.data.licences

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import io.reactivex.Single

/**
 * Created by sunny on 22.01.2018.
 * mail: mail@sunnydaydev.me
 */
@Dao
interface LicencesDao {

    @get:Query("SELECT * FROM `licenses`")
    val all: Single<List<LicenceDbe>>

    @Query("SELECT * FROM `licenses` WHERE `id` = :id")
    operator fun get(id: Long): Single<LicenceDbe>

}

@Entity(tableName = "licenses")
data class LicenceDbe(
        @PrimaryKey
        val id: Long,
        val author: String,
        val library: String,
        val type: String,
        val text: String
)