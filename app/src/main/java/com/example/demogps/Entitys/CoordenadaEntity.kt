package com.example.demogps.Entitys

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(
    tableName = "coordenadas",
    foreignKeys = [ForeignKey(
        entity = RecorridoEntity::class,
        parentColumns = ["id"],
        childColumns = ["recorridoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("recorridoId")]
)
data class CoordenadaEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val recorridoId: Long,

    val latitude: Double,

    val longitude: Double,

    val timestamp: Long
)
