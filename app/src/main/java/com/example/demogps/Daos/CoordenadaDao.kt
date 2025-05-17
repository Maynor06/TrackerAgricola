package com.example.demogps.Daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.demogps.Entitys.CoordenadaEntity
import com.example.demogps.Entitys.RecorridoEntity

@Dao
interface CoordenadaDao {

    @Insert suspend fun insert(coordenada: CoordenadaEntity): Long
    @Query("SELECT * FROM coordenadas") suspend fun getAll(): List<CoordenadaEntity>

    @Query("SELECT * FROM coordenadas WHERE recorridoId = :recorridoId")
    fun obtenerCoordenadasDeRecorrido(recorridoId: Long): LiveData<List<CoordenadaEntity>>


}