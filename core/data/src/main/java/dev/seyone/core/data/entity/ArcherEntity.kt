package dev.seyone.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.seyone.core.domain.AgeGroup
import dev.seyone.core.domain.Gender

@Entity(tableName = "archers")
data class ArcherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val clubName: String? = null,
    val gender: Gender,
    val ageGroup: AgeGroup
)