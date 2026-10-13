package app.fieldwatch.domain

import java.io.File
import java.io.RandomAccessFile
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Rough voice detector for 16 kHz mono 16-bit audio in 512-sample frames (32 ms). It is a
 * heuristic, not speech recognition: sound well above the room's quiet level, with an energy
 * mix that sounds like speech (not a low hum, not a high hiss), that rises and falls like
 * syllables. A TV or people talking outside can trigger it, and a whisper can be missed.
 */
class VoiceDetector {
    var speaking: Boolean = false
        private set

    private var floorDb = Double.NaN
    private val active = BooleanArray(ACTIVE_WINDOW)
    private val levels = DoubleArray(LEVEL_WINDOW) { Double.NaN }
    private var frames = 0
    private var lastActiveMs = 0L

    /** Feeds one frame. Returns true while a voice is considered to be present. */
    fun process(frame: ShortArray, nowMs: Long): Boolean {
        if (frame.size < 2) return speaking
        var sumSq = 0.0
        var sumDiffSq = 0.0
        for (i in frame.indices) {
            val v = frame[i].toDouble() / 32768.0
            sumSq += v * v
            if (i > 0) {
                val d = v - frame[i - 1].toDouble() / 32768.0
                sumDiffSq += d * d
            }
        }
        val rms = sqrt(sumSq / frame.size)
        val db = 20.0 * log10(rms + 1e-9)
        if (floorDb.isNaN()) floorDb = db
        // Quiet level follows the room: drops fast, rises slowly.
        floorDb = if (db < floorDb) db else floorDb + (db - floorDb) * FLOOR_RISE

        // Ratio of change between samples to energy: low for hum, high for hiss.
        val tilt = if (sumSq > 1e-12) sumDiffSq / sumSq else 0.0
        val isActive = db >= floorDb + MARGIN_DB && db >= MIN_DB && tilt in TILT_MIN..TILT_MAX

        val slot = frames % ACTIVE_WINDOW
        active[slot] = isActive
        levels[frames % LEVEL_WINDOW] = if (isActive) db else Double.NaN
        frames++
        if (isActive) lastActiveMs = nowMs

        if (!speaking) {
            if (frames >= LEVEL_WINDOW && active.count { it } >= ACTIVE_NEEDED && modulation() >= MODULATION_DB) {
                speaking = true
            }
        } else if (nowMs - lastActiveMs >= HANGOVER_MS) {
            speaking = false
            active.fill(false)
        }
        return speaking
    }

    fun reset() {
        speaking = false
        floorDb = Double.NaN
        active.fill(false)
        levels.fill(Double.NaN)
        frames = 0
        lastActiveMs = 0L
    }

    /** Spread of the loud frames' levels. Speech rises and falls; a fan or hum stays level. */
    private fun modulation(): Double {
        val loud = levels.filter { !it.isNaN() }
        if (loud.size < ACTIVE_NEEDED) return 0.0
        val mean = loud.average()
        return sqrt(loud.sumOf { (it - mean) * (it - mean) } / loud.size)
    }

    companion object {
        const val SAMPLE_RATE = 16_000
        const val FRAME_SAMPLES = 512
        const val FRAME_MS = 32L
        private const val ACTIVE_WINDOW = 8
        private const val ACTIVE_NEEDED = 5
        private const val LEVEL_WINDOW = 16
        private const val MARGIN_DB = 10.0
        private const val MIN_DB = -55.0
        private const val MODULATION_DB = 2.0
        private const val FLOOR_RISE = 0.002
        private const val HANGOVER_MS = 1_500L
        private const val TILT_MIN = 0.01
        private const val TILT_MAX = 2.5
    }
}

/** Minimal WAV (PCM 16-bit mono) helpers. */
object Wav {
    fun header(dataBytes: Int, sampleRate: Int = VoiceDetector.SAMPLE_RATE): ByteArray {
        val byteRate = sampleRate * 2
        val b = java.nio.ByteBuffer.allocate(44).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        b.put("RIFF".toByteArray()).putInt(36 + dataBytes).put("WAVE".toByteArray())
        b.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1)
        b.putInt(sampleRate).putInt(byteRate).putShort(2).putShort(16)
        b.put("data".toByteArray()).putInt(dataBytes)
        return b.array()
    }

    /** Writes samples after a placeholder header, then fixes the sizes when closed. */
    class Writer(private val file: File) {
        private val out = RandomAccessFile(file, "rw")
        var dataBytes: Int = 0
            private set

        init {
            out.setLength(0)
            out.write(header(0))
        }

        fun write(samples: ShortArray, count: Int = samples.size) {
            val b = java.nio.ByteBuffer.allocate(count * 2).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until count) b.putShort(samples[i])
            out.write(b.array())
            dataBytes += count * 2
        }

        fun close() {
            out.seek(0)
            out.write(header(dataBytes))
            out.close()
        }
    }
}

/** Keeps the newest [keep] clips in a folder and deletes the rest. */
class ClipStore(private val dir: File, private val keep: Int = 20) {
    init {
        dir.mkdirs()
    }

    fun newFile(nowMs: Long): File = File(dir, "voice_$nowMs.wav")

    fun list(): List<File> =
        (dir.listFiles { f -> f.isFile && f.name.endsWith(".wav") } ?: emptyArray())
            .sortedByDescending { it.name }

    fun prune() {
        list().drop(keep).forEach { it.delete() }
    }

    fun deleteAll() {
        list().forEach { it.delete() }
    }
}
