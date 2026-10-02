package app.fieldwatch.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SweepTest {
    private fun frame(w: Int, h: Int, fill: Int = 10): ByteArray = ByteArray(w * h) { fill.toByte() }

    private fun paint(f: ByteArray, w: Int, x0: Int, y0: Int, size: Int, v: Int = 255) {
        for (y in y0 until y0 + size) for (x in x0 until x0 + size) f[y * w + x] = v.toByte()
    }

    @Test
    fun smallBrightSpotIsAGlint() {
        val w = 160; val h = 120
        val f = frame(w, h)
        paint(f, w, 80, 60, 3)
        val g = GlintDetector.detect(f, w, h)
        assertEquals(1, g.size)
        assertEquals(0.5f, g[0].x, 0.02f)
        assertEquals(0.5f, g[0].y, 0.02f)
    }

    @Test
    fun largeBrightAreaIsNotAGlint() {
        val w = 160; val h = 120
        val f = frame(w, h)
        paint(f, w, 40, 30, 30)
        assertTrue(GlintDetector.detect(f, w, h).isEmpty())
    }

    @Test
    fun spotAmongOtherBrightSpecksIsNotAGlint() {
        val w = 160; val h = 120
        val f = frame(w, h)
        paint(f, w, 80, 60, 2)
        // Scattered single bright pixels around it, each separated from the spot by a dark gap.
        listOf(83 to 60, 80 to 63, 77 to 60, 80 to 57, 83 to 62, 77 to 62).forEach { (x, y) ->
            f[y * w + x] = 255.toByte()
        }
        assertTrue(GlintDetector.detect(f, w, h).isEmpty())
    }

    @Test
    fun evenBrightFrameHasNoGlints() {
        val w = 160; val h = 120
        assertTrue(GlintDetector.detect(frame(w, h, 240), w, h).isEmpty())
    }

    @Test
    fun twoSeparateSpotsAreTwoGlints() {
        val w = 160; val h = 120
        val f = frame(w, h)
        paint(f, w, 20, 20, 3)
        paint(f, w, 120, 90, 3)
        assertEquals(2, GlintDetector.detect(f, w, h).size)
    }

    @Test
    fun maxPoolKeepsASingleBrightPixel() {
        val w = 16; val h = 16
        val f = frame(w, h, 0)
        f[5 * w + 6] = 255.toByte()
        val pooled = GlintDetector.maxPool(f, w, h, rowStride = w, pixelStride = 1, block = 4)
        assertEquals(4, pooled.width)
        assertEquals(255, pooled.data[1 * pooled.width + 1].toInt() and 0xFF)
        assertEquals(0, pooled.data[0].toInt() and 0xFF)
    }

    @Test
    fun trackerConfirmsASteadySpotAfterFourFrames() {
        val t = GlintTracker()
        val g = listOf(Glint(0.5f, 0.5f, 4, 255))
        var out = emptyList<GlintTracker.Candidate>()
        repeat(3) { out = t.update(g) }
        assertFalse(out.single().confirmed)
        out = t.update(g)
        assertTrue(out.single().confirmed)
    }

    @Test
    fun trackerNeverConfirmsASpotThatMovesFasterThanItsMatchRadius() {
        val t = GlintTracker()
        var out = emptyList<GlintTracker.Candidate>()
        for (i in 0 until 8) {
            out = t.update(listOf(Glint(0.05f + 0.1f * i, 0.5f, 4, 255)))
        }
        assertTrue(out.none { it.confirmed })
    }

    @Test
    fun trackerConfirmsAnLedThatBlinksInTheSamePlace() {
        val t = GlintTracker()
        val g = listOf(Glint(0.3f, 0.6f, 4, 255))
        var out = emptyList<GlintTracker.Candidate>()
        // On, off, on, off...: the spot returns to the same place, so it counts as steady.
        for (i in 0 until 8) out = t.update(if (i % 2 == 0) g else emptyList())
        assertTrue(out.single().confirmed)
    }

    @Test
    fun trackerForgetsAVanishedSpot() {
        val t = GlintTracker()
        val g = listOf(Glint(0.5f, 0.5f, 4, 255))
        repeat(5) { t.update(g) }
        var out = emptyList<GlintTracker.Candidate>()
        repeat(4) { out = t.update(emptyList()) }
        assertTrue(out.isEmpty())
    }

    @Test
    fun magneticMagnitudeIsTheVectorLength() {
        val m = MagneticMeter()
        m.add(3f, 4f, 12f, 0L)
        assertEquals(13f, m.latest, 0.001f)
    }

    @Test
    fun jumpFromBaselineRaisesTheLevel() {
        val m = MagneticMeter()
        for (i in 0 until 10) m.add(0f, 0f, 50f, i * 100_000_000L)
        assertNull(m.delta())
        m.setBaselineFromRecent()
        assertEquals(MagneticLevel.NORMAL, m.level())
        m.add(0f, 0f, 65f, 1_100_000_000L)
        assertEquals(MagneticLevel.ELEVATED, m.level())
        m.add(0f, 0f, 100f, 1_200_000_000L)
        assertEquals(MagneticLevel.STRONG, m.level())
    }

    @Test
    fun steadyFieldDoesNotFluctuateButAlternatingOneDoes() {
        val steady = MagneticMeter()
        for (i in 0 until 20) steady.add(0f, 0f, 48f, i * 50_000_000L)
        assertFalse(steady.fluctuating())
        val noisy = MagneticMeter()
        for (i in 0 until 20) noisy.add(0f, 0f, if (i % 2 == 0) 46f else 50f, i * 50_000_000L)
        assertTrue(noisy.fluctuating())
    }

    @Test
    fun thinStreakIsNotAGlint() {
        val w = 160; val h = 120
        val f = frame(w, h)
        for (x in 40..51) f[60 * w + x] = 255.toByte()
        assertTrue(GlintDetector.detect(f, w, h).isEmpty())
    }

    private val glint = listOf(Glint(0.5f, 0.5f, 4, 255))

    @Test
    fun scannerConfirmsAGlintThatExistsOnlyWithTheTorchOn() {
        val s = LensScanner(startMs = 0)
        var out = emptyList<GlintTracker.Candidate>()
        for (t in 0 until 3600 step 50) {
            val g = if (s.torchShouldBeOn(t.toLong())) glint else emptyList()
            out = s.onFrame(g, t.toLong())
        }
        assertTrue(out.any { it.confirmed })
    }

    @Test
    fun scannerDropsALitLedThatShinesWithTheTorchOff() {
        val s = LensScanner(startMs = 0)
        for (t in 0 until 3600 step 50) {
            assertTrue(s.onFrame(glint, t.toLong()).isEmpty())
        }
    }

    @Test
    fun scannerIgnoresFramesWhileTheCameraSettlesAfterSwitchingOn() {
        val s = LensScanner(startMs = 0)
        var out = emptyList<GlintTracker.Candidate>()
        for (t in 0 until 3600 step 50) {
            val pos = t % 1200
            out = s.onFrame(if (pos in 500..749) glint else emptyList(), t.toLong())
        }
        assertTrue(out.isEmpty())
    }

    @Test
    fun scannerHoldsConfirmedMarksThroughTheTorchOffWindow() {
        val s = LensScanner(startMs = 0)
        var out = emptyList<GlintTracker.Candidate>()
        // Ends at t = 2800: position 400 of the third cycle, torch off.
        for (t in 0..2800 step 50) {
            val g = if (s.torchShouldBeOn(t.toLong())) glint else emptyList()
            out = s.onFrame(g, t.toLong())
        }
        assertFalse(s.torchShouldBeOn(2800))
        assertTrue(out.any { it.confirmed })
    }

    @Test
    fun torchCycleStartsOffThenOn() {
        val s = LensScanner(startMs = 1000)
        assertFalse(s.torchShouldBeOn(1000))
        assertFalse(s.torchShouldBeOn(1499))
        assertTrue(s.torchShouldBeOn(1500))
        assertTrue(s.torchShouldBeOn(2199))
        assertFalse(s.torchShouldBeOn(2200))
    }
}
