package dev.seyone.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.seyone.core.data.entity.DistanceEntity
import dev.seyone.core.data.entity.RoundEntity
import dev.seyone.core.data.entity.RoundWithDistances
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistances(distances: List<DistanceEntity>): List<Long>

    // @Transaction is strictly required when querying @Relation POJOs to prevent thread inconsistencies
    @Transaction
    @Query("SELECT * FROM rounds")
    fun getAllRoundsWithDistances(): Flow<List<RoundWithDistances>>

    @Transaction
    @Query("SELECT * FROM rounds WHERE id = :roundId")
    fun getRoundWithDistancesById(roundId: Long): Flow<RoundWithDistances?>

    @Transaction
    @Query("SELECT * FROM rounds WHERE id = :id")
    fun getRoundWithDistances(id: Long): Flow<RoundWithDistances?>
}