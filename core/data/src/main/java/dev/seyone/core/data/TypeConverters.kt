package dev.seyone.core.data

import androidx.room.TypeConverter
import dev.seyone.core.domain.BowType
import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.LocationType
import dev.seyone.core.domain.ScoringMethod
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.ShootingType
import dev.seyone.core.domain.TargetFaceSize

class ShotTypeConverters {
    @TypeConverter
    fun fromSessionType(value: SessionType): String = value.name

    @TypeConverter
    fun toSessionType(value: String): SessionType = SessionType.valueOf(value)

    @TypeConverter
    fun fromInputMethod(value: InputMethod): String = value.name

    @TypeConverter
    fun toInputMethod(value: String): InputMethod = InputMethod.valueOf(value)

    @TypeConverter
    fun fromScoringMethod(value: ScoringMethod): String = value.name

    @TypeConverter
    fun toScoringMethod(value: String): ScoringMethod = ScoringMethod.valueOf(value)

    @TypeConverter
    fun fromShootingType(value: ShootingType): String = value.name

    @TypeConverter
    fun toShootingType(value: String): ShootingType = ShootingType.valueOf(value)

    @TypeConverter
    fun fromDistanceUnit(value: DistanceUnit): String = value.name

    @TypeConverter
    fun toDistanceUnit(value: String): DistanceUnit = DistanceUnit.valueOf(value)

    @TypeConverter
    fun fromTargetFaceSize(value: TargetFaceSize): String = value.name

    @TypeConverter
    fun toTargetFaceSize(value: String): TargetFaceSize = TargetFaceSize.valueOf(value)

    @TypeConverter
    fun fromLocationType(value: LocationType): String = value.name

    @TypeConverter
    fun toLocationType(value: String): LocationType = LocationType.valueOf(value)

    @TypeConverter
    fun fromBowType(value: BowType): String = value.name

    @TypeConverter
    fun toBowType(value: String): BowType = BowType.valueOf(value)
}