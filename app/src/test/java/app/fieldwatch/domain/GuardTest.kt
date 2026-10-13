package app.fieldwatch.domain

import app.fieldwatch.domain.GuardMonitor.Kind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuardTest {
    private fun armed(vararg kinds: Kind, sensitivity: GuardMonitor.Sensitivity = GuardMonitor.Sensitivity.MEDIUM): GuardMonitor {
        val g = GuardMonitor(kinds.toSet(), sensitivity, countdownMs = 1000L, cooldownMs = 5000L)
        g.arm(0)
        g.onAccel(0f, 0f, 9.8f, 100)
        g.onLight(5f, 100)
        g.onMagnetic(20f, 0f, -40f, 100)
        g.onCharging(true, 100)
        g.onRadios(listOf(GuardMonitor.RadioObs("a", true)), 100)
        g.tick(1000)
        return g
    }

    @Test
    fun countdownDoesNotAlarm() {
        val g = GuardMonitor(Kind.values().toSet(), countdownMs = 1000L)
        g.arm(0)
        g.onAccel(0f, 0f, 9.8f, 10)
        g.onAccel(5f, 5f, 9.8f, 20)
        assertEquals(GuardMonitor.Phase.COUNTDOWN, g.phase)
        assertTrue(g.events().isEmpty())
        assertEquals(1, g.secondsLeft(500))
    }

    @Test
    fun stillPhoneStaysQuiet() {
        val g = armed(Kind.MOTION)
        repeat(20) { g.onAccel(0.05f, -0.03f, 9.82f, 2000L + it * 20) }
        assertFalse(g.alarming())
    }

    @Test
    fun movedPhoneAlarms() {
        val g = armed(Kind.MOTION)
        g.onAccel(3f, 1f, 9.8f, 2000)
        assertFalse(g.alarming())
        g.onAccel(3f, 1f, 9.8f, 2020)
        assertTrue(g.alarming())
        assertEquals(Kind.MOTION, g.events().first().kind)
    }

    @Test
    fun aSingleSpikeIsIgnored() {
        val g = armed(Kind.MOTION)
        g.onAccel(3f, 1f, 9.8f, 2000)
        g.onAccel(0f, 0f, 9.8f, 2020)
        g.onAccel(3f, 1f, 9.8f, 2040)
        assertFalse(g.alarming())
    }

    @Test
    fun lightChangeAlarmsAfterTwoReadings() {
        val g = armed(Kind.LIGHT)
        g.onLight(200f, 2000)
        assertFalse(g.alarming())
        g.onLight(210f, 2100)
        assertTrue(g.alarming())
    }

    @Test
    fun smallLightFlickerInTheDarkIsIgnored() {
        val g = armed(Kind.LIGHT)
        g.onLight(9f, 2000)
        g.onLight(11f, 2100)
        assertFalse(g.alarming())
    }

    @Test
    fun magneticChangeNeedsToStay() {
        val g = armed(Kind.MAGNETIC)
        g.onMagnetic(50f, 0f, -40f, 2000)
        g.onMagnetic(50f, 0f, -40f, 2020)
        assertFalse(g.alarming())
        g.onMagnetic(50f, 0f, -40f, 2040)
        assertTrue(g.alarming())
    }

    @Test
    fun unpluggingAlarmsOnlyIfItWasCharging() {
        val g = armed(Kind.CHARGER)
        g.onCharging(false, 2000)
        assertTrue(g.alarming())

        val h = GuardMonitor(setOf(Kind.CHARGER), countdownMs = 1000L)
        h.arm(0)
        h.onCharging(false, 100)
        h.tick(1000)
        h.onCharging(false, 2000)
        assertFalse(h.alarming())
    }

    @Test
    fun onlyNearNewRadiosAlarmAndKnownOnesDoNot() {
        val g = armed(Kind.RADIO)
        g.onRadios(listOf(GuardMonitor.RadioObs("a", true)), 2000)
        assertFalse(g.alarming())
        g.onRadios(listOf(GuardMonitor.RadioObs("far", false)), 2100)
        assertFalse(g.alarming())
        g.onRadios(listOf(GuardMonitor.RadioObs("b", true)), 2200)
        assertTrue(g.alarming())
    }

    @Test
    fun disabledKindsStaySilent() {
        val g = armed(Kind.LIGHT)
        g.onAccel(3f, 1f, 9.8f, 2000)
        g.onAccel(3f, 1f, 9.8f, 2020)
        assertFalse(g.alarming())
    }

    @Test
    fun cooldownStopsRepeatedEvents() {
        val g = armed(Kind.MOTION)
        repeat(5) { g.onAccel(3f, 1f, 9.8f, 2000L + it * 20) }
        assertEquals(1, g.events().size)
        repeat(5) { g.onAccel(3f, 1f, 9.8f, 8000L + it * 20) }
        assertEquals(2, g.events().size)
    }

    @Test
    fun highSensitivityCatchesAGentlerNudge() {
        val low = armed(Kind.MOTION, sensitivity = GuardMonitor.Sensitivity.LOW)
        val high = armed(Kind.MOTION, sensitivity = GuardMonitor.Sensitivity.HIGH)
        for (g in listOf(low, high)) {
            g.onAccel(0.6f, 0f, 9.8f, 2000)
            g.onAccel(0.6f, 0f, 9.8f, 2020)
        }
        assertFalse(low.alarming())
        assertTrue(high.alarming())
    }

    @Test
    fun disarmStopsEverything() {
        val g = armed(Kind.MOTION)
        g.disarm()
        g.onAccel(3f, 1f, 9.8f, 2000)
        g.onAccel(3f, 1f, 9.8f, 2020)
        assertFalse(g.alarming())
    }

    @Test
    fun voiceAlarmsOnlyWhenEnabledAndArmed() {
        val on = armed(Kind.VOICE)
        on.onVoiceStart(2000)
        assertTrue(on.alarming())
        val off = armed(Kind.MOTION)
        off.onVoiceStart(2000)
        assertFalse(off.alarming())
        val early = GuardMonitor(setOf(Kind.VOICE), countdownMs = 1000L)
        early.arm(0)
        early.onVoiceStart(100)
        assertFalse(early.alarming())
    }
}
