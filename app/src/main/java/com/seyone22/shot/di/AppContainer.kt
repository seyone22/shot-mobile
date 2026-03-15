package com.seyone22.shot.di

import android.content.Context
import com.seyone22.shot.data.domain.repository.ArcherRepository
import com.seyone22.shot.data.domain.repository.LocalArcherRepository
import com.seyone22.shot.data.local.ShotDatabase
import com.seyone22.shot.data.domain.repository.LocalRoundRepository
import com.seyone22.shot.data.domain.repository.LocalScoringRepository
import com.seyone22.shot.data.domain.repository.LocalSessionRepository
import com.seyone22.shot.data.domain.repository.RoundRepository
import com.seyone22.shot.data.domain.repository.ScoringRepository
import com.seyone22.shot.data.domain.repository.SessionRepository

interface AppContainer {
    val sessionRepository: SessionRepository
    val roundRepository: RoundRepository
    val scoringRepository: ScoringRepository
    val archerRepository: ArcherRepository
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
}