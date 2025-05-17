package com.example.demogps.Daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.demogps.Entitys.CoordenadaEntity
import com.example.demogps.Entitys.DeviceConRecorridos
import com.example.demogps.Entitys.DeviceEntity
import com.example.demogps.Entitys.RecorridoConCoordenadas
import com.example.demogps.Entitys.RecorridoEntity

@Dao
interface DispositivoDao {
    @Insert suspend fun insertDevice(device: DeviceEntity): Long
    @Insert suspend fun insertRecorrido(recorrido: RecorridoEntity): Long
    @Insert suspend fun insertCoordenada(coordenada: CoordenadaEntity)

    @Transaction
    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDeviceConRecorridos(deviceId: Long): DeviceConRecorridos

    @Transaction
    @Query("SELECT * FROM recorridos WHERE id= :recorridoId")
    suspend fun getRecorridoConCoordenadas(recorridoId: Long): RecorridoConCoordenadas

    @Query("SELECT * FROM devices")
    fun getAllDevices(): LiveData<List<DeviceEntity>>

    @Query("SELECT * FROM  recorridos WHERE deviceId = :deviceId")
    suspend fun getRecorridosDeDevice(deviceId: Long): List<RecorridoEntity>

    @Query("SELECT * FROM devices")
    abstract fun getAll(): LiveData<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE codigo = :address")
    abstract fun getForAdress(address: String): DeviceEntity;


}