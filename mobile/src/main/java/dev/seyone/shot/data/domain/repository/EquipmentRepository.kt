package dev.seyone.shot.data.domain.repository

import dev.seyone.shot.data.local.entity.ArrowSetEntity
import dev.seyone.shot.data.local.entity.BowComponentEntity
import dev.seyone.shot.data.local.entity.BowProfileEntity
import dev.seyone.shot.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getAllLocationsStream(): Flow<List<LocationEntity>>
    fun getLocationStream(id: Long): Flow<LocationEntity?>
    suspend fun insertLocation(location: LocationEntity): Long
    suspend fun updateLocation(location: LocationEntity)
    suspend fun deleteLocation(location: LocationEntity)
}

interface BowProfileRepository {
    fun getAllBowProfilesStream(): Flow<List<BowProfileEntity>>
    fun getBowProfileStream(id: Long): Flow<BowProfileEntity?>
    suspend fun insertBowProfile(bowProfile: BowProfileEntity): Long
    suspend fun updateBowProfile(bowProfile: BowProfileEntity)
    suspend fun deleteBowProfile(bowProfile: BowProfileEntity)
    suspend fun deleteBowProfileById(id: Long) // Added function
}

interface ArrowSetRepository {
    fun getAllArrowSetsStream(): Flow<List<ArrowSetEntity>>
    fun getArrowSetStream(id: Long): Flow<ArrowSetEntity?>
    suspend fun insertArrowSet(arrowSet: ArrowSetEntity): Long
    suspend fun updateArrowSet(arrowSet: ArrowSetEntity)
    suspend fun deleteArrowSet(arrowSet: ArrowSetEntity)
}

interface BowComponentRepository {
    /**
     * Retrieves all components attached to a specific bow profile.
     */
    fun getComponentsForBowStream(bowId: Long): Flow<List<BowComponentEntity>>

    /**
     * Inserts a new component into the database.
     * @return The ID of the newly inserted component.
     */
    suspend fun insert(component: BowComponentEntity): Long

    /**
     * Updates an existing component.
     */
    suspend fun update(component: BowComponentEntity)

    /**
     * Deletes a component from the database.
     */
    suspend fun delete(component: BowComponentEntity)
}