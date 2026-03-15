package com.seyone22.shot.data.domain.repository

import com.seyone22.shot.data.local.dao.ArcherDao
import com.seyone22.shot.data.local.entity.ArcherEntity
import kotlinx.coroutines.flow.Flow

class LocalArcherRepository(private val archerDao: ArcherDao) : ArcherRepository {
    override fun getArchersStream(): Flow<List<ArcherEntity>> = archerDao.getAllArchers()
    override suspend fun insertArcher(archer: ArcherEntity) = archerDao.insertArcher(archer)
    override suspend fun deleteArcher(archer: ArcherEntity) = archerDao.deleteArcher(archer)
}