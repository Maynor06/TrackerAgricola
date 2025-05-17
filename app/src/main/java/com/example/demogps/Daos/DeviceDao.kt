package com.example.demogps.Daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.demogps.Entitys.DeviceEntity

@Dao
interface DeviceDao {
    @Insert suspend fun insert(device: DeviceEntity): Long
    @Query("SELECT * FROM devices") suspend fun getAll(): List<DeviceEntity>
}