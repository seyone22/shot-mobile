package com.seyone22.shot.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RoundWithDistances(
    @Embedded val round: RoundEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "roundId"
    )
    val distances: List<DistanceEntity>
)