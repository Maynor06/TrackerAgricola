package com.example.demogps.Daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.demogps.Entitys.RecorridoEntity

@Dao
interface RecorridoDao {

    @Insert suspend fun insert(recorrido: RecorridoEntity): Long

    @Query("SELECT * FROM recorridos") suspend fun getAll(): List<RecorridoEntity>

    @Query("SELECT * FROM recorridos WHERE deviceId = :deviceId")
    fun obtenerRecorridosPorDispositivo(deviceId: Long): LiveData<List<RecorridoEntity>>


}