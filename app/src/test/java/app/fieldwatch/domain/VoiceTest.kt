package app.fieldwatch.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.math.PI
import kotlin.math.sin

class VoiceTest {
    private val n = VoiceDetector.FRAME_SAMPLES
    private val rate = VoiceDetector.SAMPLE_RATE

    private class Feeder(val d: VoiceDetector) {
        var frame = 0
        var everSpeaking = false
        var sample = 0L
        fun run(frames: Int, gen: (Long) -> Double): Boolean {
            repeat(frames) {
                val buf = ShortArray(VoiceDetector.FRAME_SAMPLES)
                for (i in buf.indices) {
                    val v = gen(sample++).coerceIn(-1.0, 1.0)
                    buf[i] = (v * 32767).toInt().toShort()
                }
                if (d.process(buf, frame * VoiceDetector.FRAME_MS)) everSpeaking = true
                frame++
            }
            return d.speaking
        }
    }

    private fun quiet(i: Long): Double = ((i * 7919L) % 11 - 5) / 32768.0

    private fun speechLike(i: Long): Double {
        val t = i.toDouble() / rate
        var s = 0.0
        for (h in 1..20) s += 0.02 * sin(2 * PI * 140.0 * h * t)
        val env = 0.55 + 0.45 * sin(2 * PI * 4.0 * t)
        return s * env
    }

    private fun steadyHarmonics(i: Long): Double {
        val t = i.toDouble() / rate
        var s = 0.0
        for (h in 1..20) s += 0.02 * sin(2 * PI * 140.0 * h * t)
        return s
    }

    @Test
    fun quietRoomIsNotVoice() {
        val f = Feeder(VoiceDetector())
        f.run(100, ::quiet)
        assertFalse(f.everSpeaking)
    }

    @Test
    fun speechLikeSoundIsDetectedAndThenEnds() {
        val f = Feeder(VoiceDetector())
        f.run(70, ::quiet)
        assertTrue(f.run(40, ::speechLike))
        assertFalse(f.run(80, ::quiet))
    }

    @Test
    fun steadyLowHumIsIgnored() {
        val f = Feeder(VoiceDetector())
        f.run(70, ::quiet)
        f.run(100) { i -> 0.2 * sin(2 * PI * 60.0 * i / rate) }
        assertFalse(f.everSpeaking)
    }

    @Test
    fun highHissIsIgnored() {
        val f = Feeder(VoiceDetector())
        f.run(70, ::quiet)
        f.run(100) { i ->
            val t = i.toDouble() / rate
            0.2 * sin(2 * PI * 6500.0 * t) * (0.55 + 0.45 * sin(2 * PI * 4.0 * t))
        }
        assertFalse(f.everSpeaking)
    }

    @Test
    fun steadyNoiseWithoutRiseAndFallIsIgnored() {
        val f = Feeder(VoiceDetector())
        f.run(70, ::quiet)
        f.run(100, ::steadyHarmonics)
        assertFalse(f.everSpeaking)
    }

    @Test
    fun aShortClickIsIgnored() {
        val f = Feeder(VoiceDetector())
        f.run(70, ::quiet)
        f.run(3, ::steadyHarmonics)
        f.run(60, ::quiet)
        assertFalse(f.everSpeaking)
    }

    @Test
    fun wavHeaderHasTheRightSizes() {
        val h = Wav.header(1000)
        assertEquals(44, h.size)
        assertEquals("RIFF", String(h, 0, 4))
        assertEquals("WAVE", String(h, 8, 4))
        val riff = java.nio.ByteBuffer.wrap(h, 4, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).int
        assertEquals(1036, riff)
    }

    @Test
    fun writerFixesTheSizeWhenClosed() {
        val dir = Files.createTempDirectory("clips").toFile()
        val file = File(dir, "a.wav")
        val w = Wav.Writer(file)
        w.write(ShortArray(100) { 1 })
        w.close()
        assertEquals(44L + 200L, file.length())
        val data = java.nio.ByteBuffer.wrap(file.readBytes(), 40, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).int
        assertEquals(200, data)
        dir.deleteRecursively()
    }

    @Test
    fun storeKeepsOnlyTheNewestClips() {
        val dir = Files.createTempDirectory("clips").toFile()
        val store = ClipStore(dir, keep = 3)
        for (t in 1000L..1005L) store.newFile(t).writeText("x")
        store.prune()
        assertEquals(listOf("voice_1005.wav", "voice_1004.wav", "voice_1003.wav"), store.list().map { it.name })
        store.deleteAll()
        assertTrue(store.list().isEmpty())
        dir.deleteRecursively()
    }
}
