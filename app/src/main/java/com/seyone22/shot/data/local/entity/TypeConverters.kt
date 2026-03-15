package com.seyone22.shot.data.local.entity

import androidx.room.TypeConverter

enum class SessionType { PRACTICE, COMPETITION }
enum class InputMethod { ARROW_VALUES, TARGET_FACE }
enum class ScoringMethod { METRIC_10_ZONE, METRIC_INNER_10, IMPERIAL_5_ZONE, WA_FIELD, CLOUT }
enum class ShootingType { TARGET, FIELD, CLOUT, THREED }
enum class DistanceUnit { METERS, YARDS }
enum class TargetFaceSize { CM_122, CM_80, CM_60, CM_40, CM_40_TRIPLE, CM_80_6_RING }

class ShotTypeConverters {
    @TypeConverter fun fromSessionType(value: SessionType) = value.name
    @TypeConverter fun toSessionType(value: String) = enumValueOf<SessionType>(value)

    @TypeConverter fun fromInputMethod(value: InputMethod) = value.name
    @TypeConverter fun toInputMethod(value: String) = enumValueOf<InputMethod>(value)

    @TypeConverter fun fromScoringMethod(value: ScoringMethod) = value.name
    @TypeConverter fun toScoringMethod(value: String) = enumValueOf<ScoringMethod>(value)

    @TypeConverter fun fromShootingType(value: ShootingType) = value.name
    @TypeConverter fun toShootingType(value: String) = enumValueOf<ShootingType>(value)

    @TypeConverter fun fromDistanceUnit(value: DistanceUnit) = value.name
    @TypeConverter fun toDistanceUnit(value: String) = enumValueOf<DistanceUnit>(value)

    @TypeConverter fun fromTargetFaceSize(value: TargetFaceSize) = value.name
    @TypeConverter fun toTargetFaceSize(value: String) = enumValueOf<TargetFaceSize>(value)
}