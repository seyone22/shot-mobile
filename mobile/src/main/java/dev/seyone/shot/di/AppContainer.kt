package dev.seyone.shot.di

// 1. Import the Database and Repository IMPLEMENTATIONS from your Data layer

// 2. Import the Repository INTERFACES from your Domain layer
import android.content.Context
import dev.seyone.core.data.ShotDatabase
import dev.seyone.core.data.repository.LocalArcherRepository
import dev.seyone.core.data.repository.LocalArrowSetRepository
import dev.seyone.core.data.repository.LocalBowProfileRepository
import dev.seyone.core.data.repository.LocalLocationRepository
import dev.seyone.core.data.repository.LocalRoundRepository
import dev.seyone.core.data.repository.LocalScoringRepository
import dev.seyone.core.data.repository.LocalSessionRepository
import dev.seyone.core.data.repository.OfflineBowComponentRepository
import dev.seyone.core.domain.repository.ArcherRepository
import dev.seyone.core.domain.repository.ArrowSetRepository
import dev.seyone.core.domain.repository.BowComponentRepository
import dev.seyone.core.domain.repository.BowProfileRepository
import dev.seyone.core.domain.repository.LocationRepository
import dev.seyone.core.domain.repository.RoundRepository
import dev.seyone.core.domain.repository.ScoringRepository
import dev.seyone.core.domain.repository.SessionRepository

import dev.seyone.core.data.repository.LocalSightMarkRepository
import dev.seyone.core.domain.repository.SightMarkRepository
import dev.seyone.core.data.repository.BackupRepository
import dev.seyone.core.data.repository.SettingsRepository

interface AppContainer {
    val sessionRepository: SessionRepository
    val roundRepository: RoundRepository
    val scoringRepository: ScoringRepository
    val archerRepository: ArcherRepository
    val bowProfileRepository: BowProfileRepository
    val locationRepository: LocationRepository
    val arrowSetRepository: ArrowSetRepository
    val bowComponentRepository: BowComponentRepository
    val sightMarkRepository: SightMarkRepository
    val backupRepository: BackupRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    // Initialize the Room database from the :core:data module
    private val database: ShotDatabase by lazy {
        ShotDatabase.getDatabase(context)
    }

    override val sessionRepository: SessionRepository by lazy {
        LocalSessionRepository(database.sessionDao())
    }

    override val roundRepository: RoundRepository by lazy {
        LocalRoundRepository(database.roundDao())
    }

    override val archerRepository: ArcherRepository by lazy {
        LocalArcherRepository(database.archerDao())
    }

    override val scoringRepository: ScoringRepository by lazy {
        LocalScoringRepository(database.scoringDao())
    }

    override val arrowSetRepository: ArrowSetRepository by lazy {
        LocalArrowSetRepository(database.arrowSetDao())
    }

    override val bowProfileRepository: BowProfileRepository by lazy {
        LocalBowProfileRepository(database.bowProfileDao())
    }

    override val locationRepository: LocationRepository by lazy {
        LocalLocationRepository(database.locationDao())
    }

    override val bowComponentRepository: BowComponentRepository by lazy {
        OfflineBowComponentRepository(database.bowComponentDao())
    }

    override val sightMarkRepository: SightMarkRepository by lazy {
        LocalSightMarkRepository(database.sightMarkDao())
    }

    override val backupRepository: BackupRepository by lazy {
        BackupRepository(database)
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }
}