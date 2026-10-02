package app.fieldwatch.domain

import kotlin.math.abs
import kotlin.math.sqrt

/** A tiny, very bright spot in a camera frame. [x] and [y] are 0..1 across the frame. */
data class Glint(val x: Float, val y: Float, val area: Int, val peak: Int)

/**
 * Finds pinpoint bright spots in a luma (Y) plane: the glint a lens throws back at a torch, or
 * an infrared LED seen by a sensor that lets some IR through. It cannot tell those from a lamp
 * reflected in glass or an indicator LED, so results are candidates to look at, never findings.
 */
object GlintDetector {
    class Pooled(val data: ByteArray, val width: Int, val height: Int)

    /** Shrinks a luma plane keeping the brightest pixel of each [block]x[block] square. */
    fun maxPool(
        src: ByteArray,
        width: Int,
        height: Int,
        rowStride: Int,
        pixelStride: Int,
        block: Int,
    ): Pooled {
        val ow = width / block
        val oh = height / block
        val out = ByteArray(ow * oh)
        for (oy in 0 until oh) {
            for (ox in 0 until ow) {
                var best = 0
                for (by in 0 until block) {
                    val row = (oy * block + by) * rowStride
                    for (bx in 0 until block) {
                        val v = src[row + (ox * block + bx) * pixelStride].toInt() and 0xFF
                        if (v > best) best = v
                    }
                }
                out[oy * ow + ox] = best.toByte()
            }
        }
        return Pooled(out, ow, oh)
    }

    fun detect(
        luma: ByteArray,
        width: Int,
        height: Int,
        minContrast: Int = 70,
        maxArea: Int = 30,
        maxAspect: Float = 2.5f,
    ): List<Glint> {
        val n = width * height
        if (n == 0 || luma.size < n) return emptyList()
        var sum = 0L
        for (i in 0 until n) sum += luma[i].toInt() and 0xFF
        val mean = (sum / n).toInt()
        val threshold = (mean + minContrast).coerceIn(200, 250)
        val mask = BooleanArray(n) { (luma[it].toInt() and 0xFF) >= threshold }
        val seen = BooleanArray(n)
        val stack = IntArray(n)
        val out = ArrayList<Glint>()
        for (start in 0 until n) {
            if (!mask[start] || seen[start]) continue
            var sp = 0
            stack[sp++] = start
            seen[start] = true
            var area = 0
            var sx = 0L
            var sy = 0L
            var peak = 0
            var minX = width
            var maxX = 0
            var minY = height
            var maxY = 0
            while (sp > 0) {
                val p = stack[--sp]
                val px = p % width
                val py = p / width
                area++
                sx += px
                sy += py
                peak = maxOf(peak, luma[p].toInt() and 0xFF)
                if (px < minX) minX = px
                if (px > maxX) maxX = px
                if (py < minY) minY = py
                if (py > maxY) maxY = py
                if (px > 0 && mask[p - 1] && !seen[p - 1]) { seen[p - 1] = true; stack[sp++] = p - 1 }
                if (px < width - 1 && mask[p + 1] && !seen[p + 1]) { seen[p + 1] = true; stack[sp++] = p + 1 }
                if (py > 0 && mask[p - width] && !seen[p - width]) { seen[p - width] = true; stack[sp++] = p - width }
                if (py < height - 1 && mask[p + width] && !seen[p + width]) { seen[p + width] = true; stack[sp++] = p + width }
            }
            if (area < 2 || area > maxArea) continue
            // Lens glints are small and compact. Thin streaks and ragged shapes are glossy edges.
            val bw = maxX - minX + 1
            val bh = maxY - minY + 1
            val aspect = maxOf(bw, bh).toFloat() / minOf(bw, bh)
            if (aspect > maxAspect || area < bw * bh * 0.5f) continue
            // Isolated: few other bright pixels in a ring around it, so it is not part of a lit area.
            var around = 0
            val x0 = (minX - 3).coerceAtLeast(0)
            val x1 = (maxX + 3).coerceAtMost(width - 1)
            val y0 = (minY - 3).coerceAtLeast(0)
            val y1 = (maxY + 3).coerceAtMost(height - 1)
            for (yy in y0..y1) for (xx in x0..x1) if (mask[yy * width + xx]) around++
            if (around > area * 2) continue
            out += Glint(
                x = (sx.toFloat() / area + 0.5f) / width,
                y = (sy.toFloat() / area + 0.5f) / height,
                area = area,
                peak = peak,
            )
        }
        return out
    }
}

/** Keeps glints that stay put across frames; a steady spot is more interesting than a flicker. */
class GlintTracker(
    private val matchRadius: Float = 0.04f,
    private val confirmHits: Int = 4,
    private val maxMissed: Int = 2,
) {
    private class Track(var x: Float, var y: Float, var hits: Int, var missed: Int)

    data class Candidate(val x: Float, val y: Float, val hits: Int, val confirmed: Boolean)

    private val tracks = ArrayList<Track>()

    fun update(glints: List<Glint>): List<Candidate> {
        val matched = HashSet<Track>()
        for (g in glints) {
            val near = tracks
                .filter { it !in matched }
                .minByOrNull { abs(it.x - g.x) + abs(it.y - g.y) }
            if (near != null && abs(near.x - g.x) + abs(near.y - g.y) <= matchRadius) {
                near.x = g.x
                near.y = g.y
                near.hits++
                near.missed = 0
                matched += near
            } else {
                val t = Track(g.x, g.y, 1, 0)
                tracks += t
                matched += t
            }
        }
        tracks.forEach { if (it !in matched) it.missed++ }
        tracks.removeAll { it.missed > maxMissed }
        return tracks
            .filter { it.hits >= 2 }
            .map { Candidate(it.x, it.y, it.hits, it.hits >= confirmHits) }
    }

    fun reset() = tracks.clear()
}

object GlintDiff {
    /** Keeps glints from a torch-on frame that have no match in the torch-off frames. */
    fun dropEmissive(on: List<Glint>, off: List<Glint>, radius: Float = 0.05f): List<Glint> =
        on.filter { g -> off.none { abs(it.x - g.x) + abs(it.y - g.y) <= radius } }
}

/**
 * Lens finder with the torch blinking: a lens throws the torch back, so its glint exists only
 * while the torch is on. A lit LED, lamp or screen shines with the torch off too, and is dropped.
 *
 * Each cycle is [offMs] with the torch off, then [onMs] with it on. Frames in the first
 * [settleMs] after a switch are ignored while the camera adjusts. The torch must be driven from
 * [torchShouldBeOn]; frames go to [onFrame]. The cycle starts off so a baseline exists before any
 * torch-on frame is judged.
 */
class LensScanner(
    private val startMs: Long,
    private val offMs: Long = 500,
    private val onMs: Long = 700,
    private val settleMs: Long = 250,
) {
    private val tracker = GlintTracker(matchRadius = 0.05f, confirmHits = 4, maxMissed = 3)
    private var offGlints: List<Glint> = emptyList()
    private var offWindowOpen = false
    private var haveBaseline = false
    private var shown: List<GlintTracker.Candidate> = emptyList()

    private fun position(nowMs: Long): Long = (nowMs - startMs).mod(offMs + onMs)

    fun torchShouldBeOn(nowMs: Long): Boolean = position(nowMs) >= offMs

    private fun settled(nowMs: Long): Boolean {
        val pos = position(nowMs)
        return if (pos < offMs) pos >= settleMs else pos - offMs >= settleMs
    }

    /** Candidates to show. Held through torch-off windows so the marks do not flicker. */
    fun onFrame(glints: List<Glint>, nowMs: Long): List<GlintTracker.Candidate> {
        if (!settled(nowMs)) return shown
        if (torchShouldBeOn(nowMs)) {
            offWindowOpen = false
            if (!haveBaseline) return shown
            shown = tracker.update(GlintDiff.dropEmissive(glints, offGlints))
        } else {
            offGlints = if (offWindowOpen) offGlints + glints else glints
            offWindowOpen = true
            haveBaseline = true
        }
        return shown
    }
}

enum class MagneticLevel { NORMAL, ELEVATED, STRONG }

/**
 * Phone magnetometer readings in microtesla. It senses magnets, metal and currents, not radio
 * waves, so it cannot find a transmitter. The meaningful number is the change from a baseline
 * taken in the same spot.
 */
class MagneticMeter(private val historyNanos: Long = 10_000_000_000L) {
    private val times = ArrayDeque<Long>()
    private val values = ArrayDeque<Float>()

    var baseline: Float? = null
        private set
    var latest: Float = 0f
        private set

    fun add(x: Float, y: Float, z: Float, tNanos: Long) {
        val m = sqrt(x * x + y * y + z * z)
        latest = m
        times.addLast(tNanos)
        values.addLast(m)
        while (times.isNotEmpty() && tNanos - times.first() > historyNanos) {
            times.removeFirst()
            values.removeFirst()
        }
    }

    fun history(): List<Float> = values.toList()

    private fun recent(nanos: Long): List<Float> {
        if (times.isEmpty()) return emptyList()
        val end = times.last()
        val out = ArrayList<Float>()
        for (i in times.indices) if (end - times[i] <= nanos) out += values[i]
        return out
    }

    fun setBaselineFromRecent() {
        val r = recent(1_000_000_000L)
        if (r.isNotEmpty()) baseline = r.average().toFloat()
    }

    fun clearBaseline() {
        baseline = null
    }

    fun delta(): Float? = baseline?.let { latest - it }

    fun level(): MagneticLevel {
        val d = abs(delta() ?: return MagneticLevel.NORMAL)
        return when {
            d >= STRONG_UT -> MagneticLevel.STRONG
            d >= ELEVATED_UT -> MagneticLevel.ELEVATED
            else -> MagneticLevel.NORMAL
        }
    }

    /** Spread of the last second of readings. Steady fields give near zero. */
    fun fluctuationRms(): Float {
        val r = recent(1_000_000_000L)
        if (r.size < 3) return 0f
        val mean = r.average()
        return sqrt(r.sumOf { (it - mean) * (it - mean) } / r.size).toFloat()
    }

    fun fluctuating(): Boolean = fluctuationRms() >= FLUCTUATING_UT

    companion object {
        const val ELEVATED_UT = 10f
        const val STRONG_UT = 40f
        const val FLUCTUATING_UT = 1.0f
    }
}
