package app.fieldwatch.ui.screen

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.BatteryManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.fieldwatch.domain.GuardMonitor
import app.fieldwatch.domain.GuardMonitor.Kind
import app.fieldwatch.domain.GuardMonitor.Sensitivity
import app.fieldwatch.ui.FieldwatchUi
import app.fieldwatch.ui.component.FieldwatchActionButton
import app.fieldwatch.ui.component.FieldwatchFilterChip
import app.fieldwatch.ui.component.SectionCard
import app.fieldwatch.ui.i18n.tr
import app.fieldwatch.ui.theme.SignalRed
import kotlinx.coroutines.delay

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun kindLabel(k: Kind): String = when (k) {
    Kind.MOTION -> "Movement"
    Kind.LIGHT -> "Light"
    Kind.MAGNETIC -> "Magnetic field"
    Kind.CHARGER -> "Charger"
    Kind.RADIO -> "New radios"
}

private fun sensitivityLabel(s: Sensitivity): String = when (s) {
    Sensitivity.LOW -> "Low"
    Sensitivity.MEDIUM -> "Medium"
    Sensitivity.HIGH -> "High"
}

/**
 * Watch: leave the phone still and it flags changes around it. The alarm sounds and shows on
 * this phone only. Nothing is recorded or sent anywhere.
 */
@Composable
fun GuardPanel(state: FieldwatchUi) {
    var kinds by remember { mutableStateOf(Kind.values().toSet()) }
    var sensitivity by remember { mutableStateOf(Sensitivity.MEDIUM) }
    var monitor by remember { mutableStateOf<GuardMonitor?>(null) }
    var refresh by remember { mutableLongStateOf(0L) }
    val m = monitor

    if (m == null) {
        SectionCard("Watch") {
            Text(
                tr("Leave the phone still, plugged in if you can, and it flags changes while you are away. You get 15 seconds to step out. The alarm sounds on this phone only. Nothing is recorded or sent."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(tr("Watch for"), style = MaterialTheme.typography.titleSmall)
            Kind.values().toList().chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { k ->
                        FieldwatchFilterChip(
                            selected = k in kinds,
                            onClick = { kinds = if (k in kinds) kinds - k else kinds + k },
                            label = { Text(tr(kindLabel(k))) },
                        )
                    }
                }
            }
            Text(tr("Sensitivity"), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Sensitivity.values().forEach { s ->
                    FieldwatchFilterChip(
                        selected = sensitivity == s,
                        onClick = { sensitivity = s },
                        label = { Text(tr(sensitivityLabel(s))) },
                    )
                }
            }
            Text(
                tr("The charger alarm only works if the phone is charging when you arm. A new radio only counts if it is close or matches a known signature. A fan, a passing truck or someone walking by can move a light or magnetic reading: try it first and adjust."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FieldwatchActionButton(
                onClick = {
                    val g = GuardMonitor(kinds, sensitivity)
                    g.arm(SystemClock.elapsedRealtime())
                    monitor = g
                },
                enabled = kinds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(tr("Arm")) }
        }
        return
    }

    ArmedGuard(m, state, refresh, onTick = { refresh = it }, onDisarm = { m.disarm(); monitor = null })
}

@Composable
private fun ArmedGuard(
    m: GuardMonitor,
    state: FieldwatchUi,
    refresh: Long,
    onTick: (Long) -> Unit,
    onDisarm: () -> Unit,
) {
    val context = LocalContext.current

    // Keep the screen on and dim while armed, so the sensors keep reporting.
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        val previous = window?.attributes?.screenBrightness
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.let { w ->
            val a = w.attributes
            a.screenBrightness = 0.01f
            w.attributes = a
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window?.let { w ->
                val a = w.attributes
                a.screenBrightness = previous ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                w.attributes = a
            }
        }
    }

    DisposableEffect(m) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accel = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val light = sm?.getDefaultSensor(Sensor.TYPE_LIGHT)
        val magnet = sm?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val now = SystemClock.elapsedRealtime()
                when (e.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> m.onAccel(e.values[0], e.values[1], e.values[2], now)
                    Sensor.TYPE_LIGHT -> m.onLight(e.values[0], now)
                    Sensor.TYPE_MAGNETIC_FIELD -> m.onMagnetic(e.values[0], e.values[1], e.values[2], now)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (sm != null) {
            accel?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
            light?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }
            magnet?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        }
        val battery = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                val plugged = i?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
                m.onCharging(plugged != 0, SystemClock.elapsedRealtime())
            }
        }
        val sticky = ContextCompat.registerReceiver(
            context, battery, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        sticky?.let { battery.onReceive(context, it) }
        onDispose {
            sm?.unregisterListener(listener)
            runCatching { context.unregisterReceiver(battery) }
        }
    }

    LaunchedEffect(m) {
        while (true) {
            val now = SystemClock.elapsedRealtime()
            m.tick(now)
            onTick(now)
            delay(250)
        }
    }

    LaunchedEffect(state.devices, m) {
        val heard = state.devices.filter { !it.gone }.map {
            GuardMonitor.RadioObs(it.key, near = it.rssi >= NEAR_RSSI || it.fleetIds.isNotEmpty())
        }
        m.onRadios(heard, SystemClock.elapsedRealtime())
    }

    val alarming = m.alarming()
    LaunchedEffect(alarming) {
        if (!alarming) return@LaunchedEffect
        val tone = runCatching { ToneGenerator(AudioManager.STREAM_ALARM, 100) }.getOrNull()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        try {
            while (true) {
                tone?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 1000)
                if (vibrator?.hasVibrator() == true) {
                    vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                delay(1300)
            }
        } finally {
            tone?.stopTone()
            tone?.release()
            vibrator?.cancel()
        }
    }

    val now = if (refresh > 0) refresh else SystemClock.elapsedRealtime()
    SectionCard("Watch") {
        when {
            alarming -> Text(
                tr("ALARM"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SignalRed,
            )
            m.phase == GuardMonitor.Phase.COUNTDOWN -> Text(
                tr("Arming in") + " ${m.secondsLeft(now)} s. " + tr("Step away and leave the phone still."),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            else -> Text(
                tr("Watching. Nothing has changed."),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        m.events().asReversed().forEach { e ->
            val ago = ((now - e.atMs) / 1000).coerceAtLeast(0)
            Text(
                "${tr(kindLabel(e.kind))}: ${tr(e.detail)} · ${ago}s",
                style = MaterialTheme.typography.bodyMedium,
                color = SignalRed,
            )
        }
        FieldwatchActionButton(onClick = onDisarm, modifier = Modifier.fillMaxWidth()) {
            Text(tr("Disarm"))
        }
    }
}

private const val NEAR_RSSI = -60
