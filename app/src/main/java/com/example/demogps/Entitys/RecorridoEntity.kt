package com.example.demogps.Entitys

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(
    tableName = "recorridos",
    foreignKeys = [ForeignKey(
        entity = DeviceEntity::class,
        parentColumns = ["id"],
        childColumns = ["deviceId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("deviceId")]
)
data class RecorridoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val deviceId: Long,

    val date: String
)
