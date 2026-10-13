package app.fieldwatch.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Compass maths for the Direction tool. Angles are degrees clockwise from north. */
object Heading {
    /**
     * Heading of the way the phone's back faces, from a 3x3 row-major device-to-world rotation
     * matrix (world axes: east, north, up). Held upright that is where the camera points; lying
     * flat it falls back to the top edge. Null when the matrix gives no usable direction.
     */
    fun fromRotationMatrix(r: FloatArray): Float? {
        var east = -r[2]
        var north = -r[5]
        if (sqrt(east * east + north * north) < 0.3f) {
            east = r[1]
            north = r[4]
        }
        if (sqrt(east * east + north * north) < 1e-3f) return null
        val deg = Math.toDegrees(atan2(east.toDouble(), north.toDouble())).toFloat()
        return (deg + 360f) % 360f
    }

    /** Shortest signed turn from [from] to [to]: positive means turn right (clockwise). */
    fun delta(from: Float, to: Float): Float {
        var d = (to - from) % 360f
        if (d > 180f) d -= 360f
        if (d <= -180f) d += 360f
        return d
    }
}

/**
 * Turn in place while a radio is heard and this finds the way its signal was strongest. Your body
 * blocks part of the signal, so that way is usually toward the transmitter. It is rough: indoors,
 * reflections can mislead, and it only works for radios heard several times a second (Bluetooth).
 */
class BearingSweep(private val binCount: Int = 24) {
    private val powerSum = DoubleArray(binCount)
    private val counts = IntArray(binCount)

    var samples: Int = 0
        private set

    enum class Confidence { NONE, LOW, MEDIUM, HIGH }

    data class Estimate(val bearingDeg: Float, val confidence: Confidence, val contrastDb: Float)

    fun reset() {
        powerSum.fill(0.0)
        counts.fill(0)
        samples = 0
    }

    fun add(headingDeg: Float, rssi: Int) {
        val h = ((headingDeg % 360f) + 360f) % 360f
        val bin = (h / (360f / binCount)).toInt().coerceIn(0, binCount - 1)
        powerSum[bin] += 10.0.pow(rssi / 10.0)
        counts[bin]++
        samples++
    }

    /** Share of the circle (0..1) that has at least one reading. */
    fun coverage(): Float = counts.count { it > 0 }.toFloat() / binCount

    /** Mean signal in a bin, in dBm, averaged as power. Null when nothing was heard there. */
    fun binDbm(bin: Int): Double? =
        if (counts[bin] == 0) null else 10.0 * log10(powerSum[bin] / counts[bin])

    fun bins(): Int = binCount

    /** Null until enough of the circle is covered. */
    fun estimate(): Estimate? {
        if (coverage() < MIN_COVERAGE) return null
        val smooth = DoubleArray(binCount) { Double.NaN }
        for (i in 0 until binCount) {
            var sum = 0.0
            var n = 0
            for (k in -1..1) {
                val j = (i + k + binCount) % binCount
                if (counts[j] > 0) {
                    sum += powerSum[j] / counts[j]
                    n++
                }
            }
            if (n > 0) smooth[i] = 10.0 * log10(sum / n)
        }
        val valid = smooth.filter { !it.isNaN() }.sorted()
        if (valid.isEmpty()) return null
        val peak = valid.last()
        val median = valid[valid.size / 2]
        val contrast = (peak - median).toFloat()
        var sx = 0.0
        var sy = 0.0
        for (i in 0 until binCount) {
            val v = smooth[i]
            if (v.isNaN() || v < peak - 3.0) continue
            val weight = 10.0.pow((v - peak) / 10.0)
            val angle = Math.toRadians((i + 0.5) * 360.0 / binCount)
            sx += weight * sin(angle)
            sy += weight * cos(angle)
        }
        val bearing = ((Math.toDegrees(atan2(sx, sy)).toFloat()) + 360f) % 360f
        val confidence = when {
            contrast >= 6f -> Confidence.HIGH
            contrast >= 3f -> Confidence.MEDIUM
            contrast >= 1.5f -> Confidence.LOW
            else -> Confidence.NONE
        }
        return Estimate(bearing, confidence, contrast)
    }

    companion object {
        const val MIN_COVERAGE = 0.8f
    }
}
