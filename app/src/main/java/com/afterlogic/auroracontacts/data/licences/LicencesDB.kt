package com.afterlogic.auroracontacts.data.licences

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.afterlogic.auroracontacts.application.App
import com.afterlogic.auroracontacts.data.preferences.Prefs
import dagger.Module
import dagger.Provides
import java.io.File
import java.io.FileOutputStream

/**
 * Created by sunny on 08.12.2017.
 * mail: mail@sunnydaydev.me
 */


@Database(
        version = 2,
        exportSchema = true,
        entities = [
            LicenceDbe::class
        ]
)
abstract class LicencesDB: RoomDatabase() {

    abstract val dao: LicencesDao

}

@Module
class LicencesDataModule {

    companion object {

        private const val DB = "licenses.db"

        private const val VERSION = 2

    }

    @Provides
    fun provideDataBase(context: App, prefs: Prefs): LicencesDB {

        val dbFile = context.getDatabasePath(DB)

        if (prefs.licencesVersion != VERSION || !dbFile.exists()) {

            context.copyAssetToFile("databases/$DB", dbFile)

            prefs.licencesVersion = VERSION

        }

        return Room.databaseBuilder(context, LicencesDB::class.java, DB)
                .build()

    }

    @Provides
    fun provideDao(db: LicencesDB): LicencesDao = db.dao

    private fun Context.copyAssetToFile(asset: String, target: File) {

        target.delete()

        val input = assets.open(asset)
        val output = FileOutputStream(target)

        val buffer = ByteArray(1024)

        while (true) {

            val read = input.read(buffer)
            if (read == -1) break
            output.write(buffer, 0, read)

        }

        input.close()
        output.close()

    }

}