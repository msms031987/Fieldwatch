package app.fieldwatch.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.abs

class BearingTest {
    private fun circ(a: Float, b: Float): Float = abs(Heading.delta(a, b))

    private fun matrix(r2: Float, r5: Float, r1: Float, r4: Float): FloatArray {
        val r = FloatArray(9)
        r[2] = r2
        r[5] = r5
        r[1] = r1
        r[4] = r4
        return r
    }

    @Test
    fun uprightPhoneFacingNorthAndEast() {
        // Back camera along -Z. North: -Z = (0,1,0). East: -Z = (1,0,0).
        assertEquals(0f, Heading.fromRotationMatrix(matrix(0f, -1f, 0f, 0f))!!, 0.01f)
        assertEquals(90f, Heading.fromRotationMatrix(matrix(-1f, 0f, 0f, 0f))!!, 0.01f)
        assertEquals(180f, Heading.fromRotationMatrix(matrix(0f, 1f, 0f, 0f))!!, 0.01f)
        assertEquals(270f, Heading.fromRotationMatrix(matrix(1f, 0f, 0f, 0f))!!, 0.01f)
    }

    @Test
    fun flatPhoneUsesItsTopEdge() {
        assertEquals(90f, Heading.fromRotationMatrix(matrix(0f, 0f, 1f, 0f))!!, 0.01f)
        assertNull(Heading.fromRotationMatrix(FloatArray(9)))
    }

    @Test
    fun deltaTakesTheShortWayRound() {
        assertEquals(20f, Heading.delta(350f, 10f), 0.01f)
        assertEquals(-20f, Heading.delta(10f, 350f), 0.01f)
        assertEquals(0f, Heading.delta(90f, 90f), 0.01f)
        assertEquals(-90f, Heading.delta(180f, 90f), 0.01f)
    }

    private fun sweepWithPeakAt(center: Float): BearingSweep {
        val s = BearingSweep()
        var h = 0f
        while (h < 360f) {
            s.add(h, if (circ(h, center) <= 30f) -50 else -70)
            h += 5f
        }
        return s
    }

    @Test
    fun findsTheStrongestDirection() {
        val e = sweepWithPeakAt(90f).estimate()!!
        assertEquals(BearingSweep.Confidence.HIGH, e.confidence)
        assert(circ(e.bearingDeg, 90f) <= 10f)
    }

    @Test
    fun findsAPeakAcrossNorth() {
        val e = sweepWithPeakAt(0f).estimate()!!
        assertEquals(BearingSweep.Confidence.HIGH, e.confidence)
        assert(circ(e.bearingDeg, 0f) <= 10f)
    }

    @Test
    fun evenSignalAllAroundGivesNoDirection() {
        val s = BearingSweep()
        var h = 0f
        while (h < 360f) {
            s.add(h, -60)
            h += 5f
        }
        assertEquals(BearingSweep.Confidence.NONE, s.estimate()!!.confidence)
    }

    @Test
    fun needsMostOfTheCircleBeforeEstimating() {
        val s = BearingSweep()
        var h = 0f
        while (h < 90f) {
            s.add(h, -50)
            h += 5f
        }
        assertNull(s.estimate())
        assert(s.coverage() < BearingSweep.MIN_COVERAGE)
    }

    @Test
    fun readingsInABinAreAveragedAsPower() {
        val s = BearingSweep()
        s.add(10f, -50)
        s.add(11f, -70)
        assertEquals(-53.0, s.binDbm(0)!!, 0.1)
    }
}
