package dev.seyone.core.domain.util

import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.model.SightMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SightMarkCalculatorTest {

    @Test
    fun testQuadraticInterpolationWithThreePoints() {
        // Curve: y = 0.1 * x + 2.0 (Linear case)
        val marks = listOf(
            SightMark(bowProfileId = 1L, distanceValue = 30f, distanceUnit = DistanceUnit.METERS, elevationMark = 5.0f),
            SightMark(bowProfileId = 1L, distanceValue = 50f, distanceUnit = DistanceUnit.METERS, elevationMark = 7.0f),
            SightMark(bowProfileId = 1L, distanceValue = 70f, distanceUnit = DistanceUnit.METERS, elevationMark = 9.0f)
        )

        val coeffs = SightMarkCalculator.calculateCoefficients(marks)
        assertNotNull(coeffs)

        val predicted50 = SightMarkCalculator.predictElevation(coeffs, 50f)
        assertNotNull(predicted50)
        assertEquals(7.0f, predicted50!!, 0.05f)

        val predicted40 = SightMarkCalculator.predictElevation(coeffs, 40f)
        assertNotNull(predicted40)
        assertEquals(6.0f, predicted40!!, 0.05f)
    }

    @Test
    fun testLinearInterpolationWithTwoPoints() {
        val marks = listOf(
            SightMark(bowProfileId = 1L, distanceValue = 30f, distanceUnit = DistanceUnit.METERS, elevationMark = 4.0f),
            SightMark(bowProfileId = 1L, distanceValue = 70f, distanceUnit = DistanceUnit.METERS, elevationMark = 8.0f)
        )

        val coeffs = SightMarkCalculator.calculateCoefficients(marks)
        assertNotNull(coeffs)

        val predicted50 = SightMarkCalculator.predictElevation(coeffs, 50f)
        assertNotNull(predicted50)
        assertEquals(6.0f, predicted50!!, 0.05f)
    }

    @Test
    fun testGenerateRangeCard() {
        val marks = listOf(
            SightMark(bowProfileId = 1L, distanceValue = 30f, distanceUnit = DistanceUnit.METERS, elevationMark = 4.0f),
            SightMark(bowProfileId = 1L, distanceValue = 50f, distanceUnit = DistanceUnit.METERS, elevationMark = 6.0f),
            SightMark(bowProfileId = 1L, distanceValue = 70f, distanceUnit = DistanceUnit.METERS, elevationMark = 8.0f)
        )

        val card = SightMarkCalculator.generateRangeCard(marks, listOf(30f, 50f, 60f))
        assertEquals(3, card.size)

        val item30 = card.find { it.distanceValue == 30f }
        assertNotNull(item30)
        assertEquals(false, item30!!.isCalculated) // Measured

        val item60 = card.find { it.distanceValue == 60f }
        assertNotNull(item60)
        assertEquals(true, item60!!.isCalculated) // Predicted
        assertEquals(7.0f, item60.elevationMark, 0.05f)
    }
}
