package com.seyone22.shot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender { MALE, FEMALE }

enum class AgeGroup(val label: String) {
    U12("Under 12"), U14("Under 14"), U15("Under 15"), U16("Under 16"),
    U18("Under 18"), U21("Under 21"), SENIOR("Senior"),
    FIFTY_PLUS("50+"), VETERAN("Veteran")
}

@Entity(tableName = "archers")
data class ArcherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val clubName: String? = null,
    val gender: Gender,
    val ageGroup: AgeGroup
)