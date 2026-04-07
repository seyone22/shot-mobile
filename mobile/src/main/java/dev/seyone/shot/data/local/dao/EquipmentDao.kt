package dev.seyone.shot.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.seyone.shot.data.local.entity.ArrowSetEntity
import dev.seyone.shot.data.local.entity.BowComponentEntity
import dev.seyone.shot.data.local.entity.BowProfileEntity
import dev.seyone.shot.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    @Update
    suspend fun update(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)

    @Query("SELECT * FROM locations ORDER BY isDefault DESC, name ASC")
    fun getAllLocationsStream(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id")
    fun getLocationStream(id: Long): Flow<LocationEntity?>
}

@Dao
interface BowProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bowProfile: BowProfileEntity): Long

    @Update
    suspend fun update(bowProfile: BowProfileEntity)

    @Delete
    suspend fun delete(bowProfile: BowProfileEntity)

    @Query("DELETE FROM bow_profiles WHERE id = :id") // Replace 'bow_profiles' with your actual table name
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM bow_profiles ORDER BY isDefault DESC, name ASC")
    fun getAllBowProfilesStream(): Flow<List<BowProfileEntity>>

    @Query("SELECT * FROM bow_profiles WHERE id = :id")
    fun getBowProfileStream(id: Long): Flow<BowProfileEntity?>
}

@Dao
interface ArrowSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(arrowSet: ArrowSetEntity): Long

    @Update
    suspend fun update(arrowSet: ArrowSetEntity)

    @Delete
    suspend fun delete(arrowSet: ArrowSetEntity)

    @Query("SELECT * FROM arrow_sets ORDER BY isDefault DESC, name ASC")
    fun getAllArrowSetsStream(): Flow<List<ArrowSetEntity>>

    @Query("SELECT * FROM arrow_sets WHERE id = :id")
    fun getArrowSetStream(id: Long): Flow<ArrowSetEntity?>
}

@Dao
interface BowComponentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(component: BowComponentEntity): Long

    @Update
    suspend fun update(component: BowComponentEntity)

    @Delete
    suspend fun delete(component: BowComponentEntity)

    // Gets all parts attached to a specific bow
    @Query("SELECT * FROM bow_components WHERE bowProfileId = :bowId ORDER BY category ASC")
    fun getComponentsForBow(bowId: Long): Flow<List<BowComponentEntity>>
}