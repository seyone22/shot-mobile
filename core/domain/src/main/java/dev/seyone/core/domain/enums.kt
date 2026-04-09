package dev.seyone.core.domain

enum class Gender { MALE, FEMALE }
enum class AgeGroup(val label: String) {
    U12("Under 12"), U14("Under 14"), U15("Under 15"), U16("Under 16"),
    U18("Under 18"), U21("Under 21"), SENIOR("Senior"),
    FIFTY_PLUS("50+"), VETERAN("Veteran")
}
enum class LocationType { INDOOR, OUTDOOR, FIELD, ARCHERY_3D }
enum class BowType { RECURVE, COMPOUND, BAREBOW, LONGBOW, TRADITIONAL }
enum class ComponentCategory { RISER, LIMBS, SIGHT, STRING, REST, PLUNGER, STABILIZER, CLICKER, OTHER }

/**
 * Standard units for measuring distance to the target.
 */
enum class DistanceUnit(val symbol: String) {
    METERS("m"),
    YARDS("yd")
}

/**
 * Standard target face sizes. This is critical for the visual plotting feature
 * to accurately map X/Y coordinates to score values.
 */
enum class TargetFaceSize(val description: String, val diameterCm: Int, val rings: Int) {
    CM_122("122 cm", 122, 10),
    CM_80("80 cm", 80, 10),
    CM_60("60 cm", 60, 10),
    CM_40("40 cm", 40, 10),
    SPOT_3_VERTICAL("3-Spot Vertical (40cm)", 40, 5), // Scores 6, 7, 8, 9, 10
    SPOT_3_TRIANGLE("3-Spot Triangle (40cm)", 40, 5),
    IFAA_FIELD("IFAA Field Face", 0, 5),
    CM_80_6_RING("6 Rings (80cm)", 80, 6),            // Scores 5, 6, 7, 8, 9, 10
    CM_40_TRIPLE("Triple Rings (40cm)", 40, 5),       // Scores 6, 7, 8, 9, 10
    CUSTOM("Custom", 0, 10)
}

/**
 * Defines how an arrow's value is calculated. Supports the WA inner-10 vs outer-10
 * requirements, as well as standard and IFAA scoring.
 */
enum class ScoringMethod(val description: String) {
    WA_STANDARD_10("WA 10-Zone (1-10, X)"),
    WA_INNER_10("WA Inner-10 (Compound Indoor)"),
    IMPERIAL_5_ZONE("Imperial 5-Zone (1, 3, 5, 7, 9)"),
    METRIC_10_ZONE("Metric 10-Zone (1, 2, 3, 4, 5, 6, 7, 8, 9, X)"),
    IFAA_FIELD("IFAA Field (3, 4, 5)"),
    IFAA_HUNTER("IFAA Hunter (3, 4, 5)"),
    METRIC_INNER_10("Metric Inner-10 (Compound Indoor)"),
    CUSTOM("Custom Scoring Rule")
}

/**
 * Categorizes the discipline of the round.
 */
enum class ShootingType {
    TARGET,
    FIELD,
    ARCHERY_3D,
    CLOUT,
    FLIGHT,
    BLANK_BALE // Useful for form practice/tuning where score doesn't matter
}

/**
 * Defines the context of the session to help filter analytics later.
 */
enum class SessionType {
    PRACTICE,
    COMPETITION,
    TOURNAMENT,
    LEAGUE,
    TUNING, // Used when testing arrow setups, bare shaft tuning, etc.
    SHARED_LANE // Multi-player real-time session
}

/**
 * Tracks how the data was entered into the system. This directly supports your
 * standalone logging, OCR, and visual plotting features.
 */
enum class InputMethod {
    ARROW_VALUES,     // Standard button tapping on phone
    TARGET_FACE,        // Tapping exactly where the arrow hit on a graphical target face
}