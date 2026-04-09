package dev.seyone.core.data.repository

import dev.seyone.core.data.dao.ArcherDao
import dev.seyone.core.data.mapper.toDomainModel
import dev.seyone.core.data.mapper.toEntity
import dev.seyone.core.domain.model.Archer // Important: Import the Domain Model
import dev.seyone.core.domain.repository.ArcherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalArcherRepository(private val archerDao: ArcherDao) : ArcherRepository {

    // 1. Map the Flow<List<ArcherEntity>> to Flow<List<Archer>>
    override fun getArchersStream(): Flow<List<Archer>> {
        return archerDao.getAllArchers().map { entityList ->
            // The outer 'map' transforms the Flow.
            // The inner 'map' transforms every item in the List.
            entityList.map { entity -> entity.toDomainModel() }
        }
    }

    // 2. Map Domain -> Entity before saving
    override suspend fun insertArcher(archer: Archer): Long {
        val entity = archer.toEntity()
        return archerDao.insertArcher(entity)
    }

    // 3. Map Domain -> Entity before deleting
    override suspend fun deleteArcher(archer: Archer) {
        val entity = archer.toEntity()
        archerDao.deleteArcher(entity)
    }
}