package com.example.demogps.Entitys

import androidx.room.Embedded
import androidx.room.Relation

data class RecorridoConCoordenadas(
    @Embedded val recorrido: RecorridoEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recorridoId"
    )

    val coordenadas: List<CoordenadaEntity>

)
