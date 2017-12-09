package com.afterlogic.auroracontacts.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.application.AppScope
import dagger.Module
import dagger.Provides

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */


@Database(
        version = 1,
        exportSchema = true,
        entities = [
            CalendarDbe::class
        ]
)
abstract class AuroraDB: RoomDatabase() {

    abstract fun calendars(): CalendarsDao

    abstract fun contacts(): ContactsDao

}

@Module
class DataBaseModule {

    @AppScope
    @Provides
    fun provideDataBase(context: App): AuroraDB {

        return Room.databaseBuilder(context, AuroraDB::class.java, "main-db")
                .build()

    }

    @Provides
    fun provideCalendarsDao(db: AuroraDB): CalendarsDao = db.calendars()


    @Provides
    fun provideContactsDao(db: AuroraDB): ContactsDao = db.contacts()

}