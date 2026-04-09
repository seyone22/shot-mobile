package dev.seyone.core.data.repository

import dev.seyone.core.data.dao.ArrowSetDao
import dev.seyone.core.data.dao.BowComponentDao
import dev.seyone.core.data.dao.BowProfileDao
import dev.seyone.core.data.dao.LocationDao
import dev.seyone.core.domain.model.ArrowSet
import dev.seyone.core.domain.model.BowComponent
import dev.seyone.core.domain.model.BowProfile
import dev.seyone.core.domain.model.Location
import dev.seyone.core.domain.repository.ArrowSetRepository
import dev.seyone.core.domain.repository.BowComponentRepository
import dev.seyone.core.domain.repository.BowProfileRepository
import dev.seyone.core.domain.repository.LocationRepository
import dev.seyone.core.data.mapper.* // Import your mappers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class LocalLocationRepository(private val dao: LocationDao) : LocationRepository {
    override fun getAllLocationsStream(): Flow<List<Location>> =
        dao.getAllLocationsStream().map { list ->
            list.map { entity -> entity.toDomainModel() }
        }

    override fun getLocationStream(id: Long): Flow<Location?> =
        dao.getLocationStream(id).map { entity ->
            entity?.toDomainModel()
        }

    override suspend fun insertLocation(location: Location): Long =
        dao.insert(location.toEntity())

    override suspend fun updateLocation(location: Location): Int =
        dao.update(location.toEntity())

    override suspend fun deleteLocation(location: Location): Int =
        dao.delete(location.toEntity())
}

class LocalBowProfileRepository(private val dao: BowProfileDao) : BowProfileRepository {
    override fun getAllBowProfilesStream(): Flow<List<BowProfile>> =
        dao.getAllBowProfilesStream().map { list ->
            list.map { entity -> entity.toDomainModel() }
        }

    override fun getBowProfileStream(id: Long): Flow<BowProfile?> =
        dao.getBowProfileStream(id).map { entity ->
            entity?.toDomainModel()
        }

    override suspend fun insertBowProfile(bowProfile: BowProfile): Long =
        dao.insert(bowProfile.toEntity())

    override suspend fun updateBowProfile(bowProfile: BowProfile) =
        dao.update(bowProfile.toEntity())

    override suspend fun deleteBowProfile(bowProfile: BowProfile) =
        dao.delete(bowProfile.toEntity())

    override suspend fun deleteBowProfileById(id: Long) =
        dao.deleteById(id)
}

class LocalArrowSetRepository(private val dao: ArrowSetDao) : ArrowSetRepository {
    override fun getAllArrowSetsStream(): Flow<List<ArrowSet>> =
        dao.getAllArrowSetsStream().map { list ->
            list.map { entity -> entity.toDomainModel() }
        }

    override fun getArrowSetStream(id: Long): Flow<ArrowSet?> =
        dao.getArrowSetStream(id).map { entity ->
            entity?.toDomainModel()
        }

    override suspend fun insertArrowSet(arrowSet: ArrowSet): Long =
        dao.insert(arrowSet.toEntity())

    override suspend fun updateArrowSet(arrowSet: ArrowSet) =
        dao.update(arrowSet.toEntity())

    override suspend fun deleteArrowSet(arrowSet: ArrowSet) =
        dao.delete(arrowSet.toEntity())
}

class OfflineBowComponentRepository(private val dao: BowComponentDao) : BowComponentRepository {
    override fun getComponentsForBowStream(bowId: Long): Flow<List<BowComponent>> =
        dao.getComponentsForBow(bowId).map { list ->
            list.map { entity -> entity.toDomainModel() }
        }

    override suspend fun insert(component: BowComponent): Long =
        dao.insert(component.toEntity())

    override suspend fun update(component: BowComponent) =
        dao.update(component.toEntity())

    override suspend fun delete(component: BowComponent) =
        dao.delete(component.toEntity())
}