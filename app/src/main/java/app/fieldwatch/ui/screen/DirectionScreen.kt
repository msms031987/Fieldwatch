package app.fieldwatch.ui.screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fieldwatch.domain.BearingSweep
import app.fieldwatch.domain.Heading
import app.fieldwatch.ui.FieldwatchViewModel
import app.fieldwatch.ui.NestedTabInsets
import app.fieldwatch.ui.NestedTopBar
import app.fieldwatch.ui.component.FieldwatchActionButton
import app.fieldwatch.ui.i18n.tr
import app.fieldwatch.ui.theme.BissaBlue
import app.fieldwatch.ui.theme.Slate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val COMPASS_POINTS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

private fun compassPoint(deg: Float): String =
    COMPASS_POINTS[(((deg % 360f) + 360f) % 360f / 45f + 0.5f).toInt() % 8]

/**
 * Direction finder: turn slowly in place holding the phone upright while a radio is heard
 * (Bluetooth only). Signal strength is binned by compass heading; the strongest way is a hint
 * toward the transmitter, not a bearing you can trust blindly.
 */
@Composable
fun DirectionScreen(vm: FieldwatchViewModel, onBack: () -> Unit) {
    val hunt by vm.hunt.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sweep = remember { BearingSweep() }
    var heading by remember { mutableStateOf<Float?>(null) }
    var version by remember { mutableIntStateOf(0) }
    var lastAt by remember { mutableStateOf(0L) }
    var hasSensor by remember { mutableStateOf(true) }
    var unreliable by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rot = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rot == null) {
            hasSensor = false
            return@DisposableEffect onDispose {}
        }
        val matrix = FloatArray(9)
        var smooth: Float? = null
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(matrix, e.values)
                val h = Heading.fromRotationMatrix(matrix) ?: return
                val prev = smooth
                smooth = if (prev == null) h else (prev + Heading.delta(prev, h) * 0.4f + 360f) % 360f
                heading = smooth
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                unreliable = accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW
            }
        }
        sm.registerListener(listener, rot, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sm.unregisterListener(listener) }
    }

    // One reading per new advertisement, tagged with where the phone was pointing.
    LaunchedEffect(hunt.samples.lastOrNull()?.at) {
        val s = hunt.samples.lastOrNull() ?: return@LaunchedEffect
        val h = heading ?: return@LaunchedEffect
        if (s.at <= lastAt) return@LaunchedEffect
        lastAt = s.at
        sweep.add(h, s.rssi)
        version++
    }

    val estimate = remember(version) { sweep.estimate() }
    val cur = heading
    val gold = MaterialTheme.colorScheme.primary
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        contentWindowInsets = NestedTabInsets,
        topBar = {
            NestedTopBar(
                title = "Direction",
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(hunt.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ink)
            Text(
                tr("Hold the phone upright and turn slowly in place, one full circle, taking about 20 seconds. Then turn a second time to confirm."),
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
            )
            if (!hasSensor) {
                Text(tr("This phone has no compass sensor."), color = MaterialTheme.colorScheme.error)
            }
            if (unreliable) {
                Text(
                    tr("Compass needs calibrating: wave the phone in a figure 8."),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            val bins = sweep.bins()
            Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) {
                val c = Offset(size.width / 2, size.height / 2)
                val r = min(size.width, size.height) / 2 - 24.dp.toPx()
                // Compass ring rotates so the way the phone points is always at the top.
                val rot = -(cur ?: 0f)
                val dbm = (0 until bins).mapNotNull { sweep.binDbm(it) }
                val lo = dbm.minOrNull() ?: -100.0
                val hi = (dbm.maxOrNull() ?: -40.0).coerceAtLeast(lo + 6.0)
                for (i in 0 until bins) {
                    val v = sweep.binDbm(i)
                    val a = Math.toRadians((i + 0.5) * 360.0 / bins + rot - 90.0)
                    val strength = if (v == null) 0f else ((v - lo) / (hi - lo)).toFloat().coerceIn(0f, 1f)
                    val len = if (v == null) 3.dp.toPx() else (10.dp.toPx() + strength * (r * 0.45f))
                    val start = Offset(c.x + (r - len).toFloat() * cos(a).toFloat(), c.y + (r - len).toFloat() * sin(a).toFloat())
                    val end = Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat())
                    drawLine(
                        if (v == null) Slate.copy(alpha = 0.4f) else gold.copy(alpha = 0.35f + 0.65f * strength),
                        start, end, strokeWidth = 6.dp.toPx(),
                    )
                }
                drawCircle(Slate.copy(alpha = 0.35f), r - r * 0.5f, c, style = Stroke(1.dp.toPx()))
                estimate?.takeIf { it.confidence != BearingSweep.Confidence.NONE }?.let { e ->
                    val a = Math.toRadians(e.bearingDeg + rot - 90.0)
                    val tip = Offset(c.x + (r * 0.8f) * cos(a).toFloat(), c.y + (r * 0.8f) * sin(a).toFloat())
                    drawLine(BissaBlue, c, tip, strokeWidth = 4.dp.toPx())
                    drawCircle(BissaBlue, 8.dp.toPx(), tip)
                }
                // Fixed marker: where the phone points.
                drawLine(ink, Offset(c.x, c.y - r - 14.dp.toPx()), Offset(c.x, c.y - r + 6.dp.toPx()), strokeWidth = 3.dp.toPx())
                drawCircle(ink, 3.dp.toPx(), c)
            }

            Text(
                cur?.let { "${it.roundToInt()}° ${compassPoint(it)}" } ?: "—",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ink,
            )
            Text(
                "${tr("Circle covered")}: ${(sweep.coverage() * 100).roundToInt()}% · ${sweep.samples} ${tr("readings")}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )

            val msg: String
            if (estimate == null) {
                msg = tr("Keep turning until the circle is covered.")
            } else if (estimate.confidence == BearingSweep.Confidence.NONE) {
                msg = tr("The signal is about the same in every direction. Likely very close, or reflections. Move and try again.")
            } else {
                val label = when (estimate.confidence) {
                    BearingSweep.Confidence.HIGH -> tr("Clear direction")
                    BearingSweep.Confidence.MEDIUM -> tr("Probable direction")
                    else -> tr("Weak hint")
                }
                val turn = cur?.let { Heading.delta(it, estimate.bearingDeg) }
                val turnText = when {
                    turn == null -> ""
                    abs(turn) < 15f -> " · " + tr("you are facing it")
                    turn > 0 -> " · " + tr("turn right") + " ${turn.roundToInt()}°"
                    else -> " · " + tr("turn left") + " ${(-turn).roundToInt()}°"
                }
                msg = "$label: ${estimate.bearingDeg.roundToInt()}° ${compassPoint(estimate.bearingDeg)}$turnText"
            }
            Text(msg, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = gold)

            Text(
                tr("A hint, not a bearing. Your body blocks the signal from behind, so the strongest way is usually toward the radio, but walls and reflections can fool it. Walk that way and check that the signal rises."),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )
            FieldwatchActionButton(
                onClick = { sweep.reset(); lastAt = 0L; version++ },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(tr("Start over")) }
            Spacer(Modifier.height(8.dp))
        }
    }
}
