package com.seyone22.shot.di

import android.content.Context
import com.seyone22.shot.data.domain.repository.ArcherRepository
import com.seyone22.shot.data.domain.repository.ArrowSetRepository
import com.seyone22.shot.data.domain.repository.BowComponentRepository
import com.seyone22.shot.data.domain.repository.BowProfileRepository
import com.seyone22.shot.data.domain.repository.LocalArcherRepository
import com.seyone22.shot.data.domain.repository.LocalArrowSetRepository
import com.seyone22.shot.data.domain.repository.LocalBowProfileRepository
import com.seyone22.shot.data.domain.repository.LocalLocationRepository
import com.seyone22.shot.data.domain.repository.LocalRoundRepository
import com.seyone22.shot.data.domain.repository.LocalScoringRepository
import com.seyone22.shot.data.domain.repository.LocalSessionRepository
import com.seyone22.shot.data.domain.repository.LocationRepository
import com.seyone22.shot.data.domain.repository.OfflineBowComponentRepository
import com.seyone22.shot.data.domain.repository.RoundRepository
import com.seyone22.shot.data.domain.repository.ScoringRepository
import com.seyone22.shot.data.domain.repository.SessionRepository
import com.seyone22.shot.data.local.ShotDatabase

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