package dev.seyone.core.domain.util

import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.model.SightMark
import kotlin.math.sqrt

object SightMarkCalculator {

    data class QuadraticCoefficients(
        val a: Float,
        val b: Float,
        val c: Float
    ) {
        fun predict(x: Float): Float = a * x * x + b * x + c
    }

    /**
     * Converts distance to meters for consistent math.
     */
    fun toMeters(distance: Float, unit: DistanceUnit): Float {
        return if (unit == DistanceUnit.YARDS) distance * 0.9144f else distance
    }

    /**
     * Fits a quadratic curve y = a*x^2 + b*x + c or linear curve y = m*x + c to known sight marks.
     */
    fun calculateCoefficients(marks: List<SightMark>): QuadraticCoefficients? {
        if (marks.isEmpty()) return null

        val validMarks = marks.filter { !it.isCalculated }
        if (validMarks.isEmpty()) return null

        val points = validMarks.map { Pair(toMeters(it.distanceValue, it.distanceUnit), it.elevationMark) }

        if (points.size == 1) {
            return QuadraticCoefficients(0f, 0f, points[0].second)
        }

        if (points.size == 2) {
            val (x1, y1) = points[0]
            val (x2, y2) = points[1]
            if (x2 == x1) return QuadraticCoefficients(0f, 0f, y1)
            val m = (y2 - y1) / (x2 - x1)
            val c = y1 - m * x1
            return QuadraticCoefficients(0f, m, c)
        }

        // Least Squares Quadratic Fit for N >= 3 points
        var n = points.size.toDouble()
        var sumX = 0.0
        var sumX2 = 0.0
        var sumX3 = 0.0
        var sumX4 = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumX2Y = 0.0

        for ((x, y) in points) {
            val xd = x.toDouble()
            val yd = y.toDouble()
            val x2 = xd * xd
            sumX += xd
            sumX2 += x2
            sumX3 += x2 * xd
            sumX4 += x2 * x2
            sumY += yd
            sumXY += xd * yd
            sumX2Y += x2 * yd
        }

        // Solve 3x3 linear system using Cramer's Rule
        val mainDet = sumX4 * (sumX2 * n - sumX * sumX) -
                sumX3 * (sumX3 * n - sumX * sumX2) +
                sumX2 * (sumX3 * sumX - sumX2 * sumX2)

        if (kotlin.math.abs(mainDet) < 1e-9) {
            // Degenerate case fallback to linear fit using first and last points
            val (x1, y1) = points.first()
            val (x2, y2) = points.last()
            val m = if (x2 != x1) (y2 - y1) / (x2 - x1) else 0f
            return QuadraticCoefficients(0f, m, y1 - m * x1)
        }

        val aDet = sumX2Y * (sumX2 * n - sumX * sumX) -
                sumX3 * (sumXY * n - sumX * sumY) +
                sumX2 * (sumXY * sumX - sumX2 * sumY)

        val bDet = sumX4 * (sumXY * n - sumX * sumY) -
                sumX2Y * (sumX3 * n - sumX * sumX2) +
                sumX2 * (sumX3 * sumY - sumXY * sumX2)

        val cDet = sumX4 * (sumX2 * sumY - sumXY * sumX) -
                sumX3 * (sumX3 * sumY - sumXY * sumX2) +
                sumX2Y * (sumX3 * sumX - sumX2 * sumX2)

        val a = (aDet / mainDet).toFloat()
        val b = (bDet / mainDet).toFloat()
        val c = (cDet / mainDet).toFloat()

        return QuadraticCoefficients(a, b, c)
    }

    /**
     * Predicts elevation sight mark for a specific target distance.
     * Optionally adjusts for draw weight (poundage) shifts.
     */
    fun predictElevation(
        coefficients: QuadraticCoefficients?,
        targetDistanceMeters: Float,
        originalDrawWeightLbs: Float? = null,
        currentDrawWeightLbs: Float? = null
    ): Float? {
        if (coefficients == null) return null
        var basePrediction = coefficients.predict(targetDistanceMeters)

        // Kinetic scaling adjustment if draw weight differs
        if (originalDrawWeightLbs != null && currentDrawWeightLbs != null &&
            originalDrawWeightLbs > 0 && currentDrawWeightLbs > 0 &&
            originalDrawWeightLbs != currentDrawWeightLbs
        ) {
            val scaling = sqrt(originalDrawWeightLbs / currentDrawWeightLbs)
            basePrediction *= scaling
        }

        return basePrediction
    }

    /**
     * Generates standard outdoor range lookup card (10m to 90m in 5m or 10m increments).
     */
    fun generateRangeCard(
        marks: List<SightMark>,
        distances: List<Float> = listOf(10f, 18f, 25f, 30f, 40f, 50f, 60f, 70f, 90f)
    ): List<SightMark> {
        val coeffs = calculateCoefficients(marks) ?: return emptyList()
        val knownMap = marks.associateBy { toMeters(it.distanceValue, it.distanceUnit) }
        val bowId = marks.firstOrNull()?.bowProfileId ?: 0L
        val arrowId = marks.firstOrNull()?.arrowSetId

        return distances.map { dist ->
            val existing = knownMap[dist]
            if (existing != null) {
                existing
            } else {
                val predicted = coeffs.predict(dist)
                SightMark(
                    bowProfileId = bowId,
                    arrowSetId = arrowId,
                    distanceValue = dist,
                    distanceUnit = DistanceUnit.METERS,
                    elevationMark = predicted,
                    isCalculated = true
                )
            }
        }
    }
}
