package dev.seyone.core.data.mapper

import dev.seyone.core.data.entity.ArcherEntity
import dev.seyone.core.domain.model.Archer // Use your actual domain import

// Translate Database -> Domain
fun ArcherEntity.toDomainModel(): Archer {
    return Archer(
        id = this.id,
        name = this.name,
        clubName = this.clubName,
        gender = this.gender,
        ageGroup = this.ageGroup
    )
}

// Translate Domain -> Database
fun Archer.toEntity(): ArcherEntity {
    return ArcherEntity(
        id = this.id,
        name = this.name,
        clubName = this.clubName,
        gender = this.gender,
        ageGroup = this.ageGroup
    )
}