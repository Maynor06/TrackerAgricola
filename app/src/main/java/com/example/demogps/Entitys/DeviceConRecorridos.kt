package com.example.demogps.Entitys

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceConRecorridos(
    @Embedded val device: DeviceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deviceId"
    )

    val recorridos: List<RecorridoEntity>
)
