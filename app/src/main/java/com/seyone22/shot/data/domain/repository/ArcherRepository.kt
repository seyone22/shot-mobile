package com.seyone22.shot.data.domain.repository

import com.seyone22.shot.data.local.entity.ArcherEntity
import kotlinx.coroutines.flow.Flow

interface ArcherRepository {
    fun getArchersStream(): Flow<List<ArcherEntity>>
    suspend fun insertArcher(archer: ArcherEntity)
    suspend fun deleteArcher(archer: ArcherEntity)
}