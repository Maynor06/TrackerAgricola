package com.example.demogps

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.demogps.Daos.CoordenadaDao
import com.example.demogps.Daos.DeviceDao
import com.example.demogps.Daos.DispositivoDao
import com.example.demogps.Daos.RecorridoDao
import com.example.demogps.Entitys.CoordenadaEntity
import com.example.demogps.Entitys.DeviceEntity
import com.example.demogps.Entitys.RecorridoEntity

@Database(
    entities = [DeviceEntity::class, RecorridoEntity::class, CoordenadaEntity::class],
    version = 2
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun recorridoDao(): RecorridoDao
    abstract fun coordenadaDao(): CoordenadaDao
    abstract fun dispositivoDao(): DispositivoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gps_database"
                )
                    .fallbackToDestructiveMigration() // <-- Esto destruye la base y la recrea
                    .build()
                INSTANCE = instance
                instance
            }
        }


    }



}