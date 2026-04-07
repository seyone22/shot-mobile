package com.seyone22.shot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.seyone22.shot.data.local.dao.ArcherDao
import com.seyone22.shot.data.local.dao.ArrowSetDao
import com.seyone22.shot.data.local.dao.BowComponentDao
import com.seyone22.shot.data.local.dao.BowProfileDao
import com.seyone22.shot.data.local.dao.LocationDao
import com.seyone22.shot.data.local.dao.RoundDao
import com.seyone22.shot.data.local.dao.ScoringDao
import com.seyone22.shot.data.local.dao.SessionDao
import com.seyone22.shot.data.local.entity.ArcherEntity
import com.seyone22.shot.data.local.entity.ArrowEntity
import com.seyone22.shot.data.local.entity.ArrowSetEntity
import com.seyone22.shot.data.local.entity.BowComponentEntity
import com.seyone22.shot.data.local.entity.BowProfileEntity
import com.seyone22.shot.data.local.entity.DistanceEntity
import com.seyone22.shot.data.local.entity.EndEntity
import com.seyone22.shot.data.local.entity.LocationEntity
import com.seyone22.shot.data.local.entity.RoundEntity
import com.seyone22.shot.data.local.entity.SessionEntity
import com.seyone22.shot.data.local.entity.ShotTypeConverters

@Database(
    entities = [
        SessionEntity::class,
        RoundEntity::class,
        DistanceEntity::class,
        EndEntity::class,
        ArrowEntity::class,
        ArcherEntity::class,
        LocationEntity::class,
        BowProfileEntity::class,
        ArrowSetEntity::class,
        BowComponentEntity::class   // <-- ADDED
    ],
    version = 7, // <-- Bumped to 7
    exportSchema = false
)
@TypeConverters(ShotTypeConverters::class)
abstract class ShotDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun roundDao(): RoundDao
    abstract fun scoringDao(): ScoringDao
    abstract fun archerDao(): ArcherDao
    abstract fun locationDao(): LocationDao
    abstract fun bowProfileDao(): BowProfileDao
    abstract fun arrowSetDao(): ArrowSetDao
    abstract fun bowComponentDao(): BowComponentDao // <-- ADDED

    companion object {
        @Volatile
        private var Instance: ShotDatabase? = null

        // --- MIGRATION 5 to 6 ---
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create Locations Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `locations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `isDefault` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())

                // Create Bow Profiles Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bow_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `bowType` TEXT NOT NULL, 
                        `drawWeight` REAL, 
                        `drawLength` REAL, 
                        `isDefault` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())

                // Create Arrow Sets Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `arrow_sets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `manufacturer` TEXT NOT NULL, 
                        `model` TEXT NOT NULL, 
                        `spine` INTEGER, 
                        `weight` REAL, 
                        `quantity` INTEGER NOT NULL, 
                        `isDefault` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // --- MIGRATION 6 to 7 ---
        // Creates the new table for Bow Components with Foreign Key relation to Bow Profiles
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create Bow Components Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bow_components` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `bowProfileId` INTEGER NOT NULL, 
                        `category` TEXT NOT NULL, 
                        `brand` TEXT NOT NULL, 
                        `model` TEXT NOT NULL, 
                        `price` REAL, 
                        `notes` TEXT NOT NULL,
                        FOREIGN KEY(`bowProfileId`) REFERENCES `bow_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                // Create Index for the Foreign Key (Crucial for Room performance)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bow_components_bowProfileId` ON `bow_components` (`bowProfileId`)")
            }
        }

        fun getDatabase(context: Context): ShotDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ShotDatabase::class.java, "shot_database")
                    .addCallback(DatabasePrepopulateCallback())
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7) // <-- Added MIGRATION_6_7 here
                    .fallbackToDestructiveMigration(false)
                    .build().also { Instance = it }
            }
        }

        private class DatabasePrepopulateCallback : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)

                // ==========================================
                // 1. WORLD ARCHERY (OUTDOOR)
                // ==========================================
                val waOut = "WA (Outdoor)"

                // ID 1: WA 1440 (90m) - Men
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (1, 'WA 1440 (90m)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (1, 1, 90, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (1, 2, 70, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (1, 3, 50, 'METERS', 6, 6, 'CM_80')") // Usually 3 arrows, but some WA shoots do 6. We'll use 6 arrows x 6 ends for 36 total.
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (1, 4, 30, 'METERS', 6, 6, 'CM_80')")

                // ID 2: WA 1440 (70m) - Women / Master Men
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (2, 'WA 1440 (70m)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (2, 1, 70, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (2, 2, 60, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (2, 3, 50, 'METERS', 6, 6, 'CM_80')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (2, 4, 30, 'METERS', 6, 6, 'CM_80')")

                // ID 3: WA 70m (Recurve standard)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (3, 'WA 70m (Recurve)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (3, 1, 70, 'METERS', 6, 12, 'CM_122')")

                // ID 4: WA 50m (Compound standard)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (4, 'WA 50m (Compound)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (4, 1, 50, 'METERS', 6, 12, 'CM_80_6_RING')")

                // ID 5: WA 900
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (5, 'WA 900', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (5, 1, 60, 'METERS', 6, 5, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (5, 2, 50, 'METERS', 6, 5, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (5, 3, 40, 'METERS', 6, 5, 'CM_122')")

                // ==========================================
                // 2. WORLD ARCHERY (INDOOR)
                // ==========================================
                val waIn = "WA (Indoor)"

                // ID 6: WA 18m (Recurve)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (6, 'WA 18m (Recurve)', '$waIn', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (6, 1, 18, 'METERS', 3, 20, 'CM_40')")

                // ID 7: WA 18m (Compound - Inner 10)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (7, 'WA 18m (Compound)', '$waIn', 'METRIC_INNER_10', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (7, 1, 18, 'METERS', 3, 20, 'CM_40_TRIPLE')")

                // ID 8: WA 25m
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (8, 'WA 25m', '$waIn', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (8, 1, 25, 'METERS', 3, 20, 'CM_60')")

                // ID 9: Combined WA Indoor (25m + 18m)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (9, 'Combined WA Indoor', '$waIn', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (9, 1, 25, 'METERS', 3, 20, 'CM_60')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (9, 2, 18, 'METERS', 3, 20, 'CM_40')")

                // ==========================================
                // 3. ARCHERY GB / GNAS (IMPERIAL)
                // 5-zone scoring (9, 7, 5, 3, 1) usually shot on a 122cm target face
                // ==========================================
                val gbImp = "Archery GB (Imperial)"

                // ID 10: York (144 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (10, 'York', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (10, 1, 100, 'YARDS', 6, 12, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (10, 2, 80, 'YARDS', 6, 8, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (10, 3, 60, 'YARDS', 6, 4, 'CM_122')")

                // ID 11: Hereford / Bristol I (144 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (11, 'Hereford / Bristol I', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (11, 1, 80, 'YARDS', 6, 12, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (11, 2, 60, 'YARDS', 6, 8, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (11, 3, 50, 'YARDS', 6, 4, 'CM_122')")

                // ID 12: Albion (108 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (12, 'Albion', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (12, 1, 80, 'YARDS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (12, 2, 60, 'YARDS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (12, 3, 50, 'YARDS', 6, 6, 'CM_122')")

                // ID 13: Windsor (108 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (13, 'Windsor', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (13, 1, 60, 'YARDS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (13, 2, 50, 'YARDS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (13, 3, 40, 'YARDS', 6, 6, 'CM_122')")

                // ID 14: Western (96 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (14, 'Western', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (14, 1, 60, 'YARDS', 6, 8, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (14, 2, 50, 'YARDS', 6, 8, 'CM_122')")

                // ID 15: National (72 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (15, 'National', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (15, 1, 60, 'YARDS', 6, 8, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (15, 2, 50, 'YARDS', 6, 4, 'CM_122')")

                // ID 16: Warwick (48 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (16, 'Warwick', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (16, 1, 60, 'YARDS', 6, 4, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (16, 2, 50, 'YARDS', 6, 4, 'CM_122')")

                // ==========================================
                // 4. ARCHERY GB / GNAS (METRIC)
                // 10-zone scoring, effectively shorter WA 1440s
                // ==========================================
                val gbMet = "Archery GB (Metric)"

                // ID 17: Metric I (144 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (17, 'Metric I', '$gbMet', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (17, 1, 70, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (17, 2, 60, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (17, 3, 50, 'METERS', 6, 6, 'CM_80')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (17, 4, 30, 'METERS', 6, 6, 'CM_80')")

                // ID 18: Metric II (144 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (18, 'Metric II', '$gbMet', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (18, 1, 60, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (18, 2, 50, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (18, 3, 40, 'METERS', 6, 6, 'CM_80')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (18, 4, 30, 'METERS', 6, 6, 'CM_80')")

                // ==========================================
                // 5. NFAA / USA ARCHERY
                // ==========================================
                val usa = "NFAA / USA Archery"

                // ID 19: Vegas 300 (Recurve/Compound)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (19, 'Vegas 300', '$usa', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (19, 1, 18, 'METERS', 3, 10, 'CM_40_TRIPLE')") // Technically 18m or 20yd depending on the hall, standard Vegas face
            }
        }
    }
}