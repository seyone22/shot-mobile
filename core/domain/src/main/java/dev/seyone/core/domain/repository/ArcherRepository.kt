package dev.seyone.core.domain.repository

import dev.seyone.core.domain.model.Archer
import kotlinx.coroutines.flow.Flow

interface ArcherRepository {
    fun getArchersStream(): Flow<List<Archer>>
    suspend fun insertArcher(archer: Archer): Long
    suspend fun deleteArcher(archer: Archer)
}