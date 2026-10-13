package app.fieldwatch.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProximityTest {
    @Test
    fun bandsFollowTheThresholds() {
        assertEquals(Proximity.VERY_CLOSE, Proximities.of(-40))
        assertEquals(Proximity.VERY_CLOSE, Proximities.of(-50))
        assertEquals(Proximity.CLOSE, Proximities.of(-51))
        assertEquals(Proximity.CLOSE, Proximities.of(-65))
        assertEquals(Proximity.AREA, Proximities.of(-66))
        assertEquals(Proximity.AREA, Proximities.of(-75))
        assertEquals(Proximity.FAR, Proximities.of(-76))
        assertEquals(Proximity.FAR, Proximities.of(-85))
        assertEquals(Proximity.FAINT, Proximities.of(-86))
        assertEquals(Proximity.FAINT, Proximities.of(-99))
    }

    @Test
    fun weakerSignalNeverShowsMoreBars() {
        var last = Int.MAX_VALUE
        for (rssi in -40 downTo -99) {
            val bars = Proximities.of(rssi)!!.bars
            assertTrue("bars grew at $rssi", bars <= last)
            last = bars
        }
    }

    @Test
    fun unmeasuredRadioHasNoBand() {
        assertNull(Proximities.of(127))
    }
}
