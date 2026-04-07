package dev.seyone.shot.di

import android.content.Context
import dev.seyone.shot.data.domain.repository.ArcherRepository
import dev.seyone.shot.data.domain.repository.ArrowSetRepository
import dev.seyone.shot.data.domain.repository.BowComponentRepository
import dev.seyone.shot.data.domain.repository.BowProfileRepository
import dev.seyone.shot.data.domain.repository.LocalArcherRepository
import dev.seyone.shot.data.domain.repository.LocalArrowSetRepository
import dev.seyone.shot.data.domain.repository.LocalBowProfileRepository
import dev.seyone.shot.data.domain.repository.LocalLocationRepository
import dev.seyone.shot.data.domain.repository.LocalRoundRepository
import dev.seyone.shot.data.domain.repository.LocalScoringRepository
import dev.seyone.shot.data.domain.repository.LocalSessionRepository
import dev.seyone.shot.data.domain.repository.LocationRepository
import dev.seyone.shot.data.domain.repository.OfflineBowComponentRepository
import dev.seyone.shot.data.domain.repository.RoundRepository
import dev.seyone.shot.data.domain.repository.ScoringRepository
import dev.seyone.shot.data.domain.repository.SessionRepository
import dev.seyone.shot.data.local.ShotDatabase

interface AppContainer {
    val sessionRepository: SessionRepository
    val roundRepository: RoundRepository
    val scoringRepository: ScoringRepository
    val archerRepository: ArcherRepository
    val bowProfileRepository: BowProfileRepository
    val locationRepository: LocationRepository
    val arrowSetRepository: ArrowSetRepository
    val bowComponentRepository: BowComponentRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

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

    // ... inside your DefaultAppContainer implementation ...
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
}