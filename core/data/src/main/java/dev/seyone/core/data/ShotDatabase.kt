package dev.seyone.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.seyone.core.data.dao.*
import dev.seyone.core.data.entity.*

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
        BowComponentEntity::class,
        SightMarkEntity::class
    ],
    version = 13,
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
    abstract fun bowComponentDao(): BowComponentDao
    abstract fun sightMarkDao(): SightMarkDao

    companion object {
        @Volatile
        private var Instance: ShotDatabase? = null

        // --- MIGRATION 12 to 13 ---
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add `name` column to sessions table
                db.execSQL("ALTER TABLE `sessions` ADD COLUMN `name` TEXT NOT NULL DEFAULT ''")

                // 2. Migrate existing session names stored in `notes` into `name` column
                db.execSQL("UPDATE `sessions` SET `name` = `notes` WHERE `notes` IS NOT NULL AND `notes` != ''")

                // 3. Clear legacy notes column so notes can be used for new session journaling/sight notes
                db.execSQL("UPDATE `sessions` SET `notes` = '' WHERE `name` = `notes` AND `name` != ''")
            }
        }

        // --- MIGRATION 5 to 6 ---
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `locations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `isDefault` INTEGER NOT NULL, 
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())

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
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
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

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bow_components_bowProfileId` ON `bow_components` (`bowProfileId`)")
            }
        }

        // --- MIGRATION 7 to 8 ---
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `arrow_sets` ADD COLUMN `lengthInches` REAL")
                db.execSQL("ALTER TABLE `arrow_sets` ADD COLUMN `shotCount` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `arrow_sets` ADD COLUMN `purchasePrice` REAL")
            }
        }

        // --- MIGRATION 8 to 9 ---
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val waIn = "WA (Indoor)"
                db.execSQL("INSERT OR IGNORE INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (20, 'WA 18m (Recurve - Triple Spot)', '$waIn', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (20, 1, 18, 'METERS', 3, 20, 'CM_40_TRIPLE')")
            }
        }

        // --- MIGRATION 9 to 10 ---
        // Adds WA 30m (360), WA 60m, WA 50m Barebow, Short Metric, NFAA Indoor 300, etc.
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val waOut = "WA (Outdoor)"
                val waIn = "WA (Indoor)"
                val gbMet = "Archery GB (Metric)"
                val usa = "NFAA / USA Archery"

                // ID 21: WA 30m (360) - 30m (6 ends x 6 arrows = 36 arrows)
                db.execSQL("INSERT OR IGNORE INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (21, 'WA 30m (360)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (21, 1, 30, 'METERS', 6, 6, 'CM_80')")

                // ID 22: WA 60m (Masters / U18 Recurve)
                db.execSQL("INSERT OR IGNORE INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (22, 'WA 60m (Masters/U18)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (22, 1, 60, 'METERS', 6, 12, 'CM_122')")

                // ID 23: WA 50m (Barebow)
                db.execSQL("INSERT OR IGNORE INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (23, 'WA 50m (Barebow)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (23, 1, 50, 'METERS', 6, 12, 'CM_122')")

                // ID 24: WA 18m Half Round (30 Arrows)
                db.execSQL("INSERT OR IGNORE INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (24, 'WA 18m Half Round', '$waIn', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (24, 1, 18, 'METERS', 3, 10, 'CM_40')")

                // ID 25: Short Metric (72 Arrows)
                db.execSQL("INSERT OR IGNORE INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (25, 'Short Metric', '$gbMet', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (25, 1, 50, 'METERS', 6, 6, 'CM_80')")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (25, 2, 30, 'METERS', 6, 6, 'CM_80')")

                // ID 26: NFAA Indoor 300
                db.execSQL("INSERT OR IGNORE INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (26, 'NFAA Indoor 300', '$usa', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (26, 1, 18, 'METERS', 5, 12, 'CM_40')")

                // ID 27: 30m Practice (36 Arrows)
                db.execSQL("INSERT OR IGNORE INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (27, '30m Practice (36 Arrows)', 'Practice', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT OR IGNORE INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (27, 1, 30, 'METERS', 6, 6, 'CM_80')")
            }
        }

        // --- MIGRATION 10 to 11 ---
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `sessions` ADD COLUMN `archerId` INTEGER DEFAULT NULL")
            }
        }

        // --- MIGRATION 11 to 12 ---
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sight_marks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `bowProfileId` INTEGER NOT NULL,
                        `arrowSetId` INTEGER,
                        `drawWeightLbs` REAL,
                        `distanceValue` REAL NOT NULL,
                        `distanceUnit` TEXT NOT NULL,
                        `elevationMark` REAL NOT NULL,
                        `windageMark` REAL,
                        `notes` TEXT NOT NULL,
                        FOREIGN KEY(`bowProfileId`) REFERENCES `bow_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sight_marks_bowProfileId` ON `sight_marks` (`bowProfileId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sight_marks_arrowSetId` ON `sight_marks` (`arrowSetId`)")
            }
        }

        fun getDatabase(context: Context): ShotDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ShotDatabase::class.java, "shot_database")
                    .addMigrations(
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabasePrepopulateCallback())
                    .build().also { Instance = it }
            }
        }

        private class DatabasePrepopulateCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // ==========================================
                // 1. WORLD ARCHERY (OUTDOOR)
                // ==========================================
                val waOut = "WA (Outdoor)"

                // ID 1: WA 1440 (90m) - Men
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (1, 'WA 1440 (90m)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (1, 1, 90, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (1, 2, 70, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (1, 3, 50, 'METERS', 6, 6, 'CM_80')")
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

                // ID 21: WA 30m (360)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (21, 'WA 30m (360)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (21, 1, 30, 'METERS', 6, 6, 'CM_80')")

                // ID 22: WA 60m (Masters/U18)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (22, 'WA 60m (Masters/U18)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (22, 1, 60, 'METERS', 6, 12, 'CM_122')")

                // ID 23: WA 50m (Barebow)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (23, 'WA 50m (Barebow)', '$waOut', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (23, 1, 50, 'METERS', 6, 12, 'CM_122')")

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

                // ID 20: WA 18m (Recurve - Triple Spot)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (20, 'WA 18m (Recurve - Triple Spot)', '$waIn', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (20, 1, 18, 'METERS', 3, 20, 'CM_40_TRIPLE')")

                // ID 24: WA 18m Half Round
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (24, 'WA 18m Half Round', '$waIn', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (24, 1, 18, 'METERS', 3, 10, 'CM_40')")

                // ==========================================
                // 3. ARCHERY GB / GNAS (IMPERIAL)
                // ==========================================
                val gbImp = "Archery GB (Imperial)"

                // ID 10: York (144 arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (10, 'York', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (10, 1, 100, 'YARDS', 6, 12, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (10, 2, 80, 'YARDS', 6, 8, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (10, 3, 60, 'YARDS', 6, 4, 'CM_122')")

                // ID 11: Hereford / Bristol I
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (11, 'Hereford / Bristol I', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (11, 1, 80, 'YARDS', 6, 12, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (11, 2, 60, 'YARDS', 6, 8, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (11, 3, 50, 'YARDS', 6, 4, 'CM_122')")

                // ID 12: Albion
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (12, 'Albion', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (12, 1, 80, 'YARDS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (12, 2, 60, 'YARDS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (12, 3, 50, 'YARDS', 6, 6, 'CM_122')")

                // ID 13: Windsor
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (13, 'Windsor', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (13, 1, 60, 'YARDS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (13, 2, 50, 'YARDS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (13, 3, 40, 'YARDS', 6, 6, 'CM_122')")

                // ID 14: Western
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (14, 'Western', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (14, 1, 60, 'YARDS', 6, 8, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (14, 2, 50, 'YARDS', 6, 8, 'CM_122')")

                // ID 15: National
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (15, 'National', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (15, 1, 60, 'YARDS', 6, 8, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (15, 2, 50, 'YARDS', 6, 4, 'CM_122')")

                // ID 16: Warwick
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (16, 'Warwick', '$gbImp', 'IMPERIAL_5_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (16, 1, 60, 'YARDS', 6, 4, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (16, 2, 50, 'YARDS', 6, 4, 'CM_122')")

                // ==========================================
                // 4. ARCHERY GB / GNAS (METRIC)
                // ==========================================
                val gbMet = "Archery GB (Metric)"

                // ID 17: Metric I
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (17, 'Metric I', '$gbMet', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (17, 1, 70, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (17, 2, 60, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (17, 3, 50, 'METERS', 6, 6, 'CM_80')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (17, 4, 30, 'METERS', 6, 6, 'CM_80')")

                // ID 18: Metric II
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (18, 'Metric II', '$gbMet', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (18, 1, 60, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (18, 2, 50, 'METERS', 6, 6, 'CM_122')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (18, 3, 40, 'METERS', 6, 6, 'CM_80')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (18, 4, 30, 'METERS', 6, 6, 'CM_80')")

                // ID 25: Short Metric
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (25, 'Short Metric', '$gbMet', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (25, 1, 50, 'METERS', 6, 6, 'CM_80')")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (25, 2, 30, 'METERS', 6, 6, 'CM_80')")

                // ==========================================
                // 5. NFAA / USA ARCHERY
                // ==========================================
                val usa = "NFAA / USA Archery"

                // ID 19: Vegas 300
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (19, 'Vegas 300', '$usa', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (19, 1, 18, 'METERS', 3, 10, 'CM_40_TRIPLE')")

                // ID 26: NFAA Indoor 300
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (26, 'NFAA Indoor 300', '$usa', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (26, 1, 18, 'METERS', 5, 12, 'CM_40')")

                // ID 27: 30m Practice (36 Arrows)
                db.execSQL("INSERT INTO rounds (id, name, category, scoringMethod, shootingType, isCustom) VALUES (27, '30m Practice (36 Arrows)', 'Practice', 'METRIC_10_ZONE', 'TARGET', 0)")
                db.execSQL("INSERT INTO distances (roundId, sequenceOrder, distanceValue, distanceUnit, arrowsPerEnd, numberOfEnds, targetFaceSize) VALUES (27, 1, 30, 'METERS', 6, 6, 'CM_80')")
            }
        }
    }
}