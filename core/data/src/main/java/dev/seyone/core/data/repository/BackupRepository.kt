package dev.seyone.core.data.repository

import dev.seyone.core.data.ShotDatabase
import dev.seyone.core.data.entity.*
import dev.seyone.core.domain.AgeGroup
import dev.seyone.core.domain.BowType
import dev.seyone.core.domain.ComponentCategory
import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.Gender
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.LocationType
import dev.seyone.core.domain.ScoringMethod
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.ShootingType
import dev.seyone.core.domain.TargetFaceSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

data class BackupSummary(
    val sessionsCount: Int = 0,
    val archersCount: Int = 0,
    val bowProfilesCount: Int = 0,
    val arrowSetsCount: Int = 0,
    val locationsCount: Int = 0,
    val endsCount: Int = 0,
    val arrowsCount: Int = 0
)

class BackupRepository(private val database: ShotDatabase) {

    suspend fun getBackupSummary(): BackupSummary = withContext(Dispatchers.IO) {
        val sessions = database.sessionDao().getAllSessionsSync()
        val archers = database.archerDao().getAllArchersSync()
        val bows = database.bowProfileDao().getAllBowProfilesSync()
        val arrows = database.arrowSetDao().getAllArrowSetsSync()
        val locations = database.locationDao().getAllLocationsSync()

        var endsCount = 0
        var arrowsCount = 0
        for (session in sessions) {
            val endsWithArrows = database.scoringDao().getEndsWithArrowsForSessionSync(session.id)
            endsCount += endsWithArrows.size
            arrowsCount += endsWithArrows.sumOf { it.arrows.size }
        }

        BackupSummary(
            sessionsCount = sessions.size,
            archersCount = archers.size,
            bowProfilesCount = bows.size,
            arrowSetsCount = arrows.size,
            locationsCount = locations.size,
            endsCount = endsCount,
            arrowsCount = arrowsCount
        )
    }

    suspend fun exportBackup(outputStream: OutputStream): BackupSummary = withContext(Dispatchers.IO) {
        val sessions = database.sessionDao().getAllSessionsSync()
        val archers = database.archerDao().getAllArchersSync()
        val locations = database.locationDao().getAllLocationsSync()
        val bows = database.bowProfileDao().getAllBowProfilesSync()
        val arrowSets = database.arrowSetDao().getAllArrowSetsSync()
        val components = database.bowComponentDao().getAllComponentsSync()
        val sightMarks = database.sightMarkDao().getAllSightMarksSync()
        val roundsWithDistances = database.roundDao().getAllRoundsWithDistancesSync().filter { it.round.isCustom }

        val rootJson = JSONObject()
        rootJson.put("version", 1)
        rootJson.put("timestamp", System.currentTimeMillis())
        rootJson.put("appName", "Shot Mobile")

        // Archers
        val archersArray = JSONArray()
        for (a in archers) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("name", a.name)
            obj.put("clubName", a.clubName ?: "")
            obj.put("gender", a.gender.name)
            obj.put("ageGroup", a.ageGroup.name)
            archersArray.put(obj)
        }
        rootJson.put("archers", archersArray)

        // Locations
        val locationsArray = JSONArray()
        for (l in locations) {
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("name", l.name)
            obj.put("type", l.type.name)
            obj.put("isDefault", l.isDefault)
            obj.put("notes", l.notes)
            locationsArray.put(obj)
        }
        rootJson.put("locations", locationsArray)

        // Bow Profiles
        val bowsArray = JSONArray()
        for (b in bows) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("name", b.name)
            obj.put("bowType", b.bowType.name)
            obj.put("drawWeight", b.drawWeight ?: JSONObject.NULL)
            obj.put("drawLength", b.drawLength ?: JSONObject.NULL)
            obj.put("isDefault", b.isDefault)
            obj.put("notes", b.notes)
            bowsArray.put(obj)
        }
        rootJson.put("bowProfiles", bowsArray)

        // Arrow Sets
        val arrowSetsArray = JSONArray()
        for (a in arrowSets) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("name", a.name)
            obj.put("manufacturer", a.manufacturer)
            obj.put("model", a.model)
            obj.put("spine", a.spine ?: JSONObject.NULL)
            obj.put("weight", a.weight ?: JSONObject.NULL)
            obj.put("quantity", a.quantity)
            obj.put("isDefault", a.isDefault)
            obj.put("notes", a.notes)
            obj.put("lengthInches", a.lengthInches ?: JSONObject.NULL)
            obj.put("shotCount", a.shotCount)
            obj.put("purchasePrice", a.purchasePrice ?: JSONObject.NULL)
            arrowSetsArray.put(obj)
        }
        rootJson.put("arrowSets", arrowSetsArray)

        // Bow Components
        val componentsArray = JSONArray()
        for (c in components) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("bowProfileId", c.bowProfileId)
            obj.put("category", c.category.name)
            obj.put("brand", c.brand)
            obj.put("model", c.model)
            obj.put("price", c.price ?: JSONObject.NULL)
            obj.put("notes", c.notes)
            componentsArray.put(obj)
        }
        rootJson.put("bowComponents", componentsArray)

        // Sight Marks
        val sightMarksArray = JSONArray()
        for (sm in sightMarks) {
            val obj = JSONObject()
            obj.put("id", sm.id)
            obj.put("bowProfileId", sm.bowProfileId)
            sm.arrowSetId?.let { obj.put("arrowSetId", it) }
            sm.drawWeightLbs?.let { obj.put("drawWeightLbs", it.toDouble()) }
            obj.put("distanceValue", sm.distanceValue.toDouble())
            obj.put("distanceUnit", sm.distanceUnit.name)
            obj.put("elevationMark", sm.elevationMark.toDouble())
            sm.windageMark?.let { obj.put("windageMark", it.toDouble()) }
            obj.put("notes", sm.notes)
            sightMarksArray.put(obj)
        }
        rootJson.put("sightMarks", sightMarksArray)

        // Custom Rounds
        val roundsArray = JSONArray()
        for (rw in roundsWithDistances) {
            val obj = JSONObject()
            obj.put("id", rw.round.id)
            obj.put("name", rw.round.name)
            obj.put("category", rw.round.category)
            obj.put("scoringMethod", rw.round.scoringMethod.name)
            obj.put("shootingType", rw.round.shootingType.name)
            obj.put("isCustom", rw.round.isCustom)

            val distArray = JSONArray()
            for (d in rw.distances) {
                val distObj = JSONObject()
                distObj.put("id", d.id)
                distObj.put("roundId", d.roundId)
                distObj.put("sequenceOrder", d.sequenceOrder)
                distObj.put("distanceValue", d.distanceValue)
                distObj.put("distanceUnit", d.distanceUnit.name)
                distObj.put("arrowsPerEnd", d.arrowsPerEnd)
                distObj.put("numberOfEnds", d.numberOfEnds)
                distObj.put("targetFaceSize", d.targetFaceSize.name)
                distArray.put(distObj)
            }
            obj.put("distances", distArray)
            roundsArray.put(obj)
        }
        rootJson.put("customRounds", roundsArray)

        // Sessions with Ends & Arrows
        var totalEndsCount = 0
        var totalArrowsCount = 0
        val sessionsArray = JSONArray()
        for (s in sessions) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("roundId", s.roundId)
            obj.put("archerId", s.archerId ?: JSONObject.NULL)
            obj.put("bowId", s.bowId ?: JSONObject.NULL)
            obj.put("arrowId", s.arrowId ?: JSONObject.NULL)
            obj.put("locationId", s.locationId ?: JSONObject.NULL)
            obj.put("sessionType", s.sessionType.name)
            obj.put("inputMethod", s.inputMethod.name)
            obj.put("numberOfArchers", s.numberOfArchers)
            obj.put("arrowsPerEnd", s.arrowsPerEnd)
            obj.put("notes", s.notes)
            obj.put("timestamp", s.timestamp)

            val endsWithArrows = database.scoringDao().getEndsWithArrowsForSessionSync(s.id)
            totalEndsCount += endsWithArrows.size
            val endsArray = JSONArray()
            for (ew in endsWithArrows) {
                val endObj = JSONObject()
                endObj.put("id", ew.end.id)
                endObj.put("sessionId", ew.end.sessionId)
                endObj.put("sequenceOrder", ew.end.sequenceOrder)

                totalArrowsCount += ew.arrows.size
                val arrowsArr = JSONArray()
                for (arr in ew.arrows) {
                    val arrObj = JSONObject()
                    arrObj.put("id", arr.id)
                    arrObj.put("endId", arr.endId)
                    arrObj.put("sequenceOrder", arr.sequenceOrder)
                    arrObj.put("scoreValue", arr.scoreValue)
                    arrowsArr.put(arrObj)
                }
                endObj.put("arrows", arrowsArr)
                endsArray.put(endObj)
            }
            obj.put("ends", endsArray)
            sessionsArray.put(obj)
        }
        rootJson.put("sessions", sessionsArray)

        outputStream.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
        outputStream.flush()

        BackupSummary(
            sessionsCount = sessions.size,
            archersCount = archers.size,
            bowProfilesCount = bows.size,
            arrowSetsCount = arrowSets.size,
            locationsCount = locations.size,
            endsCount = totalEndsCount,
            arrowsCount = totalArrowsCount
        )
    }

    suspend fun importBackup(inputStream: InputStream, mergeMode: Boolean): BackupSummary = withContext(Dispatchers.IO) {
        val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

        if (jsonString.isBlank()) {
            throw IllegalArgumentException("Backup file is empty (0 bytes).")
        }

        val rootJson = try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("File is not a valid JSON document: ${e.localizedMessage}")
        }

        // Validate JSON schema before running any destructive DB queries
        val hasValidData = rootJson.has("version") || rootJson.has("appName") ||
                rootJson.has("sessions") || rootJson.has("archers") ||
                rootJson.has("bowProfiles") || rootJson.has("arrowSets")

        if (!hasValidData) {
            throw IllegalArgumentException("Invalid backup file: Missing required Shot Mobile database elements.")
        }

        val version = rootJson.optInt("version", 1)
        if (version > 1) {
            throw IllegalArgumentException("Unsupported backup version: $version")
        }

        if (!mergeMode) {
            database.openHelper.writableDatabase.execSQL("DELETE FROM sight_marks")
            database.openHelper.writableDatabase.execSQL("DELETE FROM arrows")
            database.openHelper.writableDatabase.execSQL("DELETE FROM ends")
            database.openHelper.writableDatabase.execSQL("DELETE FROM sessions")
            database.openHelper.writableDatabase.execSQL("DELETE FROM bow_components")
            database.openHelper.writableDatabase.execSQL("DELETE FROM arrow_sets")
            database.openHelper.writableDatabase.execSQL("DELETE FROM bow_profiles")
            database.openHelper.writableDatabase.execSQL("DELETE FROM locations")
            database.openHelper.writableDatabase.execSQL("DELETE FROM archers")
            database.openHelper.writableDatabase.execSQL("DELETE FROM distances WHERE roundId IN (SELECT id FROM rounds WHERE isCustom = 1)")
            database.openHelper.writableDatabase.execSQL("DELETE FROM rounds WHERE isCustom = 1")
        }

        var importedSessions = 0
        var importedArchers = 0
        var importedBows = 0
        var importedArrowSets = 0
        var importedLocations = 0
        var importedEnds = 0
        var importedArrows = 0

        val archerIdMap = mutableMapOf<Long, Long>()
        val locationIdMap = mutableMapOf<Long, Long>()
        val bowIdMap = mutableMapOf<Long, Long>()
        val arrowIdMap = mutableMapOf<Long, Long>()
        val roundIdMap = mutableMapOf<Long, Long>()

        // Import Archers
        val archersArr = rootJson.optJSONArray("archers")
        if (archersArr != null) {
            for (i in 0 until archersArr.length()) {
                val obj = archersArr.getJSONObject(i)
                val oldId = obj.optLong("id", 0L)
                val archer = ArcherEntity(
                    id = if (mergeMode) 0L else oldId,
                    name = obj.getString("name"),
                    clubName = obj.optString("clubName", "").ifBlank { null },
                    gender = Gender.valueOf(obj.optString("gender", Gender.MALE.name)),
                    ageGroup = AgeGroup.valueOf(obj.optString("ageGroup", AgeGroup.SENIOR.name))
                )
                val insertedId = database.archerDao().insertArcher(archer)
                if (oldId != 0L) archerIdMap[oldId] = if (mergeMode) insertedId else oldId
                importedArchers++
            }
        }

        // Import Locations
        val locationsArr = rootJson.optJSONArray("locations")
        if (locationsArr != null) {
            for (i in 0 until locationsArr.length()) {
                val obj = locationsArr.getJSONObject(i)
                val oldId = obj.optLong("id", 0L)
                val loc = LocationEntity(
                    id = if (mergeMode) 0L else oldId,
                    name = obj.getString("name"),
                    type = LocationType.valueOf(obj.optString("type", LocationType.OUTDOOR.name)),
                    isDefault = obj.optBoolean("isDefault", false),
                    notes = obj.optString("notes", "")
                )
                val insertedId = database.locationDao().insert(loc)
                if (oldId != 0L) locationIdMap[oldId] = if (mergeMode) insertedId else oldId
                importedLocations++
            }
        }

        // Import Bow Profiles
        val bowsArr = rootJson.optJSONArray("bowProfiles")
        if (bowsArr != null) {
            for (i in 0 until bowsArr.length()) {
                val obj = bowsArr.getJSONObject(i)
                val oldId = obj.optLong("id", 0L)
                val bow = BowProfileEntity(
                    id = if (mergeMode) 0L else oldId,
                    name = obj.getString("name"),
                    bowType = BowType.valueOf(obj.optString("bowType", BowType.RECURVE.name)),
                    drawWeight = if (obj.isNull("drawWeight")) null else obj.optDouble("drawWeight").toFloat(),
                    drawLength = if (obj.isNull("drawLength")) null else obj.optDouble("drawLength").toFloat(),
                    isDefault = obj.optBoolean("isDefault", false),
                    notes = obj.optString("notes", "")
                )
                val insertedId = database.bowProfileDao().insert(bow)
                if (oldId != 0L) bowIdMap[oldId] = if (mergeMode) insertedId else oldId
                importedBows++
            }
        }

        // Import Arrow Sets
        val arrowSetsArr = rootJson.optJSONArray("arrowSets")
        if (arrowSetsArr != null) {
            for (i in 0 until arrowSetsArr.length()) {
                val obj = arrowSetsArr.getJSONObject(i)
                val oldId = obj.optLong("id", 0L)
                val arrowSet = ArrowSetEntity(
                    id = if (mergeMode) 0L else oldId,
                    name = obj.getString("name"),
                    manufacturer = obj.optString("manufacturer", ""),
                    model = obj.optString("model", ""),
                    spine = if (obj.isNull("spine")) null else obj.optInt("spine"),
                    weight = if (obj.isNull("weight")) null else obj.optDouble("weight").toFloat(),
                    quantity = obj.optInt("quantity", 12),
                    isDefault = obj.optBoolean("isDefault", false),
                    notes = obj.optString("notes", ""),
                    lengthInches = if (obj.isNull("lengthInches")) null else obj.optDouble("lengthInches").toFloat(),
                    shotCount = obj.optInt("shotCount", 0),
                    purchasePrice = if (obj.isNull("purchasePrice")) null else obj.optDouble("purchasePrice")
                )
                val insertedId = database.arrowSetDao().insert(arrowSet)
                if (oldId != 0L) arrowIdMap[oldId] = if (mergeMode) insertedId else oldId
                importedArrowSets++
            }
        }

        // Import Bow Components
        val componentsArr = rootJson.optJSONArray("bowComponents")
        if (componentsArr != null) {
            for (i in 0 until componentsArr.length()) {
                val obj = componentsArr.getJSONObject(i)
                val oldBowId = obj.getLong("bowProfileId")
                val targetBowId = bowIdMap[oldBowId] ?: oldBowId
                val comp = BowComponentEntity(
                    id = if (mergeMode) 0L else obj.optLong("id", 0L),
                    bowProfileId = targetBowId,
                    category = ComponentCategory.valueOf(obj.optString("category", ComponentCategory.RISER.name)),
                    brand = obj.optString("brand", ""),
                    model = obj.optString("model", ""),
                    price = if (obj.isNull("price")) null else obj.optDouble("price"),
                    notes = obj.optString("notes", "")
                )
                database.bowComponentDao().insert(comp)
            }
        }

        // Import Custom Rounds
        val roundsArr = rootJson.optJSONArray("customRounds")
        if (roundsArr != null) {
            for (i in 0 until roundsArr.length()) {
                val obj = roundsArr.getJSONObject(i)
                val oldRoundId = obj.optLong("id", 0L)
                val roundEntity = RoundEntity(
                    id = if (mergeMode) 0L else oldRoundId,
                    name = obj.getString("name"),
                    category = obj.optString("category", "Custom"),
                    scoringMethod = ScoringMethod.valueOf(obj.optString("scoringMethod", ScoringMethod.METRIC_10_ZONE.name)),
                    shootingType = ShootingType.valueOf(obj.optString("shootingType", ShootingType.TARGET.name)),
                    isCustom = true
                )
                val insertedRoundId = database.roundDao().insertRound(roundEntity)
                val finalRoundId = if (oldRoundId != 0L && !mergeMode) oldRoundId else insertedRoundId
                if (oldRoundId != 0L) roundIdMap[oldRoundId] = finalRoundId

                val distArr = obj.optJSONArray("distances")
                if (distArr != null) {
                    val distEntities = mutableListOf<DistanceEntity>()
                    for (d in 0 until distArr.length()) {
                        val dObj = distArr.getJSONObject(d)
                        distEntities.add(
                            DistanceEntity(
                                id = if (mergeMode) 0L else dObj.optLong("id", 0L),
                                roundId = finalRoundId,
                                sequenceOrder = dObj.optInt("sequenceOrder", d + 1),
                                distanceValue = dObj.optInt("distanceValue", 18),
                                distanceUnit = DistanceUnit.valueOf(dObj.optString("distanceUnit", DistanceUnit.METERS.name)),
                                arrowsPerEnd = dObj.optInt("arrowsPerEnd", 6),
                                numberOfEnds = dObj.optInt("numberOfEnds", 12),
                                targetFaceSize = TargetFaceSize.valueOf(dObj.optString("targetFaceSize", TargetFaceSize.CM_40.name))
                            )
                        )
                    }
                    database.roundDao().insertDistances(distEntities)
                }
            }
        }

        // Import Sessions with Ends & Arrows
        val sessionsArr = rootJson.optJSONArray("sessions")
        if (sessionsArr != null) {
            for (i in 0 until sessionsArr.length()) {
                val obj = sessionsArr.getJSONObject(i)
                val oldSessionId = obj.optLong("id", 0L)
                val oldRoundId = obj.getLong("roundId")
                val oldArcherId = if (obj.isNull("archerId")) null else obj.optLong("archerId")
                val oldBowId = if (obj.isNull("bowId")) null else obj.optLong("bowId")
                val oldArrowId = if (obj.isNull("arrowId")) null else obj.optLong("arrowId")
                val oldLocationId = if (obj.isNull("locationId")) null else obj.optLong("locationId")

                val targetSessionId = if (mergeMode) 0L else oldSessionId
                val sessionEntity = SessionEntity(
                    id = targetSessionId,
                    roundId = roundIdMap[oldRoundId] ?: oldRoundId,
                    archerId = if (oldArcherId != null) (archerIdMap[oldArcherId] ?: oldArcherId) else null,
                    bowId = if (oldBowId != null) (bowIdMap[oldBowId] ?: oldBowId) else null,
                    arrowId = if (oldArrowId != null) (arrowIdMap[oldArrowId] ?: oldArrowId) else null,
                    locationId = if (oldLocationId != null) (locationIdMap[oldLocationId] ?: oldLocationId) else null,
                    sessionType = SessionType.valueOf(obj.optString("sessionType", SessionType.PRACTICE.name)),
                    inputMethod = InputMethod.valueOf(obj.optString("inputMethod", InputMethod.ARROW_VALUES.name)),
                    numberOfArchers = obj.optInt("numberOfArchers", 1),
                    arrowsPerEnd = obj.optInt("arrowsPerEnd", 6),
                    notes = obj.optString("notes", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
                val insertedSessionId = database.sessionDao().insertSession(sessionEntity)
                val finalSessionId = if (oldSessionId != 0L && !mergeMode) oldSessionId else insertedSessionId
                importedSessions++

                val endsArr = obj.optJSONArray("ends")
                if (endsArr != null) {
                    for (e in 0 until endsArr.length()) {
                        val endObj = endsArr.getJSONObject(e)
                        val endId = if (mergeMode) 0L else endObj.optLong("id", 0L)
                        val endEntity = EndEntity(
                            id = endId,
                            sessionId = finalSessionId,
                            sequenceOrder = endObj.optInt("sequenceOrder", e + 1)
                        )
                        val insertedEndId = database.scoringDao().insertEnd(endEntity)
                        val finalEndId = if (endId != 0L && !mergeMode) endId else insertedEndId
                        importedEnds++

                        val arrowsArr = endObj.optJSONArray("arrows")
                        if (arrowsArr != null) {
                            val arrowEntities = mutableListOf<ArrowEntity>()
                            for (a in 0 until arrowsArr.length()) {
                                val arrObj = arrowsArr.getJSONObject(a)
                                arrowEntities.add(
                                    ArrowEntity(
                                        id = if (mergeMode) 0L else arrObj.optLong("id", 0L),
                                        endId = finalEndId,
                                        sequenceOrder = arrObj.optInt("sequenceOrder", a + 1),
                                        scoreValue = arrObj.optInt("scoreValue", 0)
                                    )
                                )
                                importedArrows++
                            }
                            database.scoringDao().insertArrows(arrowEntities)
                        }
                    }
                }
            }
        }

        // Import Sight Marks
        if (rootJson.has("sightMarks")) {
            val smArray = rootJson.getJSONArray("sightMarks")
            for (i in 0 until smArray.length()) {
                val obj = smArray.getJSONObject(i)
                val rawBowId = obj.getLong("bowProfileId")
                val finalBowId = if (mergeMode) (bowIdMap[rawBowId] ?: rawBowId) else rawBowId

                val rawArrowId = if (obj.has("arrowSetId") && !obj.isNull("arrowSetId")) obj.getLong("arrowSetId") else null
                val finalArrowId = if (rawArrowId != null) (if (mergeMode) (arrowIdMap[rawArrowId] ?: rawArrowId) else rawArrowId) else null

                val smEntity = SightMarkEntity(
                    id = if (mergeMode) 0L else obj.optLong("id", 0L),
                    bowProfileId = finalBowId,
                    arrowSetId = finalArrowId,
                    drawWeightLbs = if (obj.has("drawWeightLbs")) obj.getDouble("drawWeightLbs").toFloat() else null,
                    distanceValue = obj.getDouble("distanceValue").toFloat(),
                    distanceUnit = try { DistanceUnit.valueOf(obj.getString("distanceUnit")) } catch (e: Exception) { DistanceUnit.METERS },
                    elevationMark = obj.getDouble("elevationMark").toFloat(),
                    windageMark = if (obj.has("windageMark")) obj.getDouble("windageMark").toFloat() else null,
                    notes = obj.optString("notes", "")
                )
                database.sightMarkDao().insertSightMark(smEntity)
            }
        }

        BackupSummary(
            sessionsCount = importedSessions,
            archersCount = importedArchers,
            bowProfilesCount = importedBows,
            arrowSetsCount = importedArrowSets,
            locationsCount = importedLocations,
            endsCount = importedEnds,
            arrowsCount = importedArrows
        )
    }
}
