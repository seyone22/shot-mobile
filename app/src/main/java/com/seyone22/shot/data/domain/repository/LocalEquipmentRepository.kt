package com.seyone22.shot.data.domain.repository

import com.seyone22.shot.data.local.dao.ArrowSetDao
import com.seyone22.shot.data.local.dao.BowComponentDao
import com.seyone22.shot.data.local.dao.BowProfileDao
import com.seyone22.shot.data.local.dao.LocationDao
import com.seyone22.shot.data.local.entity.ArrowSetEntity
import com.seyone22.shot.data.local.entity.BowComponentEntity
import com.seyone22.shot.data.local.entity.BowProfileEntity
import com.seyone22.shot.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

class LocalLocationRepository(private val dao: LocationDao) : LocationRepository {
    override fun getAllLocationsStream(): Flow<List<LocationEntity>> = dao.getAllLocationsStream()
    override fun getLocationStream(id: Long): Flow<LocationEntity?> = dao.getLocationStream(id)
    override suspend fun insertLocation(location: LocationEntity): Long = dao.insert(location)
    override suspend fun updateLocation(location: LocationEntity) = dao.update(location)
    override suspend fun deleteLocation(location: LocationEntity) = dao.delete(location)
}

class LocalBowProfileRepository(private val dao: BowProfileDao) : BowProfileRepository {
    override fun getAllBowProfilesStream(): Flow<List<BowProfileEntity>> =
        dao.getAllBowProfilesStream()

    override fun getBowProfileStream(id: Long): Flow<BowProfileEntity?> =
        dao.getBowProfileStream(id)

    override suspend fun insertBowProfile(bowProfile: BowProfileEntity): Long =
        dao.insert(bowProfile)

    override suspend fun updateBowProfile(bowProfile: BowProfileEntity) = dao.update(bowProfile)
    override suspend fun deleteBowProfile(bowProfile: BowProfileEntity) = dao.delete(bowProfile)
    override suspend fun deleteBowProfileById(id: Long) = dao.deleteById(id)
}

class LocalArrowSetRepository(private val dao: ArrowSetDao) : ArrowSetRepository {
    override fun getAllArrowSetsStream(): Flow<List<ArrowSetEntity>> = dao.getAllArrowSetsStream()
    override fun getArrowSetStream(id: Long): Flow<ArrowSetEntity?> = dao.getArrowSetStream(id)
    override suspend fun insertArrowSet(arrowSet: ArrowSetEntity): Long = dao.insert(arrowSet)
    override suspend fun updateArrowSet(arrowSet: ArrowSetEntity) = dao.update(arrowSet)
    override suspend fun deleteArrowSet(arrowSet: ArrowSetEntity) = dao.delete(arrowSet)
}

class OfflineBowComponentRepository(
    private val dao: BowComponentDao
) : BowComponentRepository {

    override fun getComponentsForBowStream(bowId: Long): Flow<List<BowComponentEntity>> {
        return dao.getComponentsForBow(bowId)
    }

    override suspend fun insert(component: BowComponentEntity): Long {
        return dao.insert(component)
    }

    override suspend fun update(component: BowComponentEntity) {
        dao.update(component)
    }

    override suspend fun delete(component: BowComponentEntity) {
        dao.delete(component)
    }
}