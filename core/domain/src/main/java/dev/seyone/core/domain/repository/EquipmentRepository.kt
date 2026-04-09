package dev.seyone.core.domain.repository

import dev.seyone.core.domain.model.ArrowSet
import dev.seyone.core.domain.model.BowComponent
import dev.seyone.core.domain.model.BowProfile
import dev.seyone.core.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getAllLocationsStream(): Flow<List<Location>>
    fun getLocationStream(id: Long): Flow<Location?>
    suspend fun insertLocation(location: Location): Long
    suspend fun updateLocation(location: Location): Int
    suspend fun deleteLocation(location: Location): Int
}

interface BowProfileRepository {
    fun getAllBowProfilesStream(): Flow<List<BowProfile>>
    fun getBowProfileStream(id: Long): Flow<BowProfile?>
    suspend fun insertBowProfile(bowProfile: BowProfile): Long
    suspend fun updateBowProfile(bowProfile: BowProfile): Int
    suspend fun deleteBowProfile(bowProfile: BowProfile): Int
    suspend fun deleteBowProfileById(id: Long): Int
}

interface ArrowSetRepository {
    fun getAllArrowSetsStream(): Flow<List<ArrowSet>>
    fun getArrowSetStream(id: Long): Flow<ArrowSet?>
    suspend fun insertArrowSet(arrowSet: ArrowSet): Long
    suspend fun updateArrowSet(arrowSet: ArrowSet): Int
    suspend fun deleteArrowSet(arrowSet: ArrowSet): Int
}

interface BowComponentRepository {
    fun getComponentsForBowStream(bowId: Long): Flow<List<BowComponent>>
    suspend fun insert(component: BowComponent): Long
    suspend fun update(component: BowComponent): Int
    suspend fun delete(component: BowComponent): Int
}