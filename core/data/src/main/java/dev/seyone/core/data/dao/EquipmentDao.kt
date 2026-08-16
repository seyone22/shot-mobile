package dev.seyone.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.seyone.core.data.entity.ArrowSetEntity
import dev.seyone.core.data.entity.BowComponentEntity
import dev.seyone.core.data.entity.BowProfileEntity
import dev.seyone.core.data.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    @Update
    suspend fun update(location: LocationEntity): Int

    @Delete
    suspend fun delete(location: LocationEntity): Int

    @Query("SELECT * FROM locations ORDER BY isDefault DESC, name ASC")
    fun getAllLocationsStream(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations ORDER BY isDefault DESC, name ASC")
    suspend fun getAllLocationsSync(): List<LocationEntity>

    @Query("SELECT * FROM locations WHERE id = :id")
    fun getLocationStream(id: Long): Flow<LocationEntity?>
}

@Dao
interface BowProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bowProfile: BowProfileEntity): Long

    @Update
    suspend fun update(bowProfile: BowProfileEntity): Int

    @Delete
    suspend fun delete(bowProfile: BowProfileEntity): Int

    @Query("DELETE FROM bow_profiles WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM bow_profiles ORDER BY isDefault DESC, name ASC")
    fun getAllBowProfilesStream(): Flow<List<BowProfileEntity>>

    @Query("SELECT * FROM bow_profiles ORDER BY isDefault DESC, name ASC")
    suspend fun getAllBowProfilesSync(): List<BowProfileEntity>

    @Query("SELECT * FROM bow_profiles WHERE id = :id")
    fun getBowProfileStream(id: Long): Flow<BowProfileEntity?>
}

@Dao
interface ArrowSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(arrowSet: ArrowSetEntity): Long

    @Update
    suspend fun update(arrowSet: ArrowSetEntity): Int

    @Delete
    suspend fun delete(arrowSet: ArrowSetEntity): Int

    @Query("SELECT * FROM arrow_sets ORDER BY isDefault DESC, name ASC")
    fun getAllArrowSetsStream(): Flow<List<ArrowSetEntity>>

    @Query("SELECT * FROM arrow_sets ORDER BY isDefault DESC, name ASC")
    suspend fun getAllArrowSetsSync(): List<ArrowSetEntity>

    @Query("SELECT * FROM arrow_sets WHERE id = :id")
    fun getArrowSetStream(id: Long): Flow<ArrowSetEntity?>
}

@Dao
interface BowComponentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(component: BowComponentEntity): Long

    @Update
    suspend fun update(component: BowComponentEntity): Int

    @Delete
    suspend fun delete(component: BowComponentEntity): Int

    // Gets all parts attached to a specific bow
    @Query("SELECT * FROM bow_components WHERE bowProfileId = :bowId ORDER BY category ASC")
    fun getComponentsForBow(bowId: Long): Flow<List<BowComponentEntity>>

    @Query("SELECT * FROM bow_components ORDER BY category ASC")
    fun getAllComponentsStream(): Flow<List<BowComponentEntity>>

    @Query("SELECT * FROM bow_components ORDER BY category ASC")
    suspend fun getAllComponentsSync(): List<BowComponentEntity>
}