package app.fieldwatch.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Guard watch: leave the phone still in a room and it notices changes. Pure logic, fed by the
 * UI with sensor readings. It trusts nothing it cannot measure: it only says something changed
 * since arming, not who or why.
 */
class GuardMonitor(
    private val enabled: Set<Kind>,
    private val sensitivity: Sensitivity = Sensitivity.MEDIUM,
    private val countdownMs: Long = 15_000L,
    private val cooldownMs: Long = 8_000L,
) {
    enum class Kind { MOTION, LIGHT, MAGNETIC, CHARGER, RADIO, VOICE }

    enum class Sensitivity(val motion: Float, val magnetic: Float, val lightFactor: Float) {
        LOW(1.5f, 20f, 1.0f),
        MEDIUM(0.8f, 10f, 0.6f),
        HIGH(0.4f, 5f, 0.35f),
    }

    enum class Phase { IDLE, COUNTDOWN, ARMED }

    data class Event(val kind: Kind, val atMs: Long, val detail: String)

    /** A radio heard now. [near] is a strong signal or a named pattern hit. */
    data class RadioObs(val key: String, val near: Boolean)

    var phase: Phase = Phase.IDLE
        private set
    private var startedMs = 0L
    private val events = ArrayList<Event>()
    private val lastFired = HashMap<Kind, Long>()

    private var ax = 0f
    private var ay = 0f
    private var az = 0f
    private var accelSeen = false
    private var lux = 0f
    private var luxSeen = false
    private var mag = 0f
    private var magSeen = false
    private var charging: Boolean? = null
    private var chargingAtArm: Boolean? = null
    private val known = HashSet<String>()

    private var motionHits = 0
    private var lightHits = 0
    private var magHits = 0

    fun events(): List<Event> = events.toList()

    fun alarming(): Boolean = phase == Phase.ARMED && events.isNotEmpty()

    fun arm(nowMs: Long) {
        phase = Phase.COUNTDOWN
        startedMs = nowMs
        events.clear()
        lastFired.clear()
        accelSeen = false
        luxSeen = false
        magSeen = false
        known.clear()
        motionHits = 0
        lightHits = 0
        magHits = 0
        chargingAtArm = null
    }

    fun disarm() {
        phase = Phase.IDLE
    }

    fun secondsLeft(nowMs: Long): Int =
        if (phase != Phase.COUNTDOWN) 0 else ((countdownMs - (nowMs - startedMs) + 999) / 1000).toInt().coerceAtLeast(0)

    /** Moves countdown to armed once its time is up. Call on a timer. */
    fun tick(nowMs: Long) {
        if (phase == Phase.COUNTDOWN && nowMs - startedMs >= countdownMs) {
            phase = Phase.ARMED
            chargingAtArm = charging
        }
    }

    private fun fire(kind: Kind, nowMs: Long, detail: String) {
        if (phase != Phase.ARMED || kind !in enabled) return
        val last = lastFired[kind]
        if (last != null && nowMs - last < cooldownMs) return
        lastFired[kind] = nowMs
        events.add(Event(kind, nowMs, detail))
    }

    // While counting down the baseline follows the readings. Once armed it stays fixed.
    fun onAccel(x: Float, y: Float, z: Float, nowMs: Long) {
        when (phase) {
            Phase.IDLE -> return
            Phase.COUNTDOWN -> {
                if (!accelSeen) { ax = x; ay = y; az = z; accelSeen = true }
                else { ax += (x - ax) * 0.2f; ay += (y - ay) * 0.2f; az += (z - az) * 0.2f }
            }
            Phase.ARMED -> {
                if (!accelSeen) return
                val d = sqrt((x - ax) * (x - ax) + (y - ay) * (y - ay) + (z - az) * (z - az))
                motionHits = if (d >= sensitivity.motion) motionHits + 1 else 0
                if (motionHits >= 2) fire(Kind.MOTION, nowMs, "The phone was moved")
            }
        }
    }

    fun onLight(value: Float, nowMs: Long) {
        when (phase) {
            Phase.IDLE -> return
            Phase.COUNTDOWN -> {
                lux = if (!luxSeen) value else lux + (value - lux) * 0.3f
                luxSeen = true
            }
            Phase.ARMED -> {
                if (!luxSeen) return
                val limit = max(12f, lux * sensitivity.lightFactor)
                lightHits = if (abs(value - lux) >= limit) lightHits + 1 else 0
                if (lightHits >= 2) {
                    fire(Kind.LIGHT, nowMs, if (value > lux) "The light got brighter" else "The light got darker")
                }
            }
        }
    }

    fun onMagnetic(x: Float, y: Float, z: Float, nowMs: Long) {
        val m = sqrt(x * x + y * y + z * z)
        when (phase) {
            Phase.IDLE -> return
            Phase.COUNTDOWN -> {
                mag = if (!magSeen) m else mag + (m - mag) * 0.2f
                magSeen = true
            }
            Phase.ARMED -> {
                if (!magSeen) return
                magHits = if (abs(m - mag) >= sensitivity.magnetic) magHits + 1 else 0
                if (magHits >= 3) fire(Kind.MAGNETIC, nowMs, "The magnetic field changed")
            }
        }
    }

    fun onCharging(isCharging: Boolean, nowMs: Long) {
        charging = isCharging
        if (phase == Phase.ARMED && chargingAtArm == true && !isCharging) {
            fire(Kind.CHARGER, nowMs, "The charger was unplugged")
        }
    }

    /** Called once each time a voice starts. */
    fun onVoiceStart(nowMs: Long) {
        fire(Kind.VOICE, nowMs, "A voice was heard")
    }

    fun onRadios(heard: List<RadioObs>, nowMs: Long) {
        when (phase) {
            Phase.IDLE -> return
            Phase.COUNTDOWN -> heard.forEach { known.add(it.key) }
            Phase.ARMED -> for (r in heard) {
                if (r.key in known) continue
                known.add(r.key)
                if (r.near) fire(Kind.RADIO, nowMs, "A new radio appeared close by")
            }
        }
    }
}
