package com.ajoclub.ajopos.model.data

import androidx.room.Embedded
import androidx.room.Relation

data class NotaWithDetail(
    @Embedded val nota: Nota,

    @Relation(
        parentColumn = "local_id",
        entityColumn = "nota_local_id"
    )
    val detailNota: List<DetailNota>
)
