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
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.core.content.FileProvider
import app.fieldwatch.domain.ClipStore
import app.fieldwatch.domain.VoiceDetector
import app.fieldwatch.domain.Wav
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.currentCoroutineContext
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
    Kind.VOICE -> "Voices"
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
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("guard", Context.MODE_PRIVATE) }
    var record by remember { mutableStateOf(false) }
    var showLegal by remember { mutableStateOf(false) }
    var micDenied by remember { mutableStateOf(false) }
    var clipVersion by remember { mutableIntStateOf(0) }
    val store = remember { ClipStore(File(context.filesDir, "guard_clips")) }
    val m = monitor

    fun startMonitor() {
        val g = GuardMonitor(kinds, sensitivity)
        g.arm(SystemClock.elapsedRealtime())
        monitor = g
    }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        micDenied = !ok
        if (ok) startMonitor()
    }

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
            if (Kind.VOICE in kinds) {
                FieldwatchFilterChip(
                    selected = record,
                    onClick = {
                        if (record) {
                            record = false
                        } else if (prefs.getBoolean("legal_ok", false)) {
                            record = true
                        } else {
                            showLegal = true
                        }
                    },
                    label = { Text(tr("Record clips when a voice is heard (keeps the last 20)")) },
                )
                Text(
                    tr("Voice detection is approximate: a TV or people talking outside can trigger it, and a whisper can be missed. Clips stay on this phone."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (micDenied) {
                Text(
                    tr("The microphone permission was not granted, so the guard did not start."),
                    style = MaterialTheme.typography.bodySmall,
                    color = SignalRed,
                )
            }
            Text(
                tr("The charger alarm only works if the phone is charging when you arm. A new radio only counts if it is close or matches a known signature. A fan, a passing truck or someone walking by can move a light or magnetic reading: try it first and adjust."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FieldwatchActionButton(
                onClick = {
                    val needsMic = Kind.VOICE in kinds
                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    micDenied = false
                    if (needsMic && !hasMic) micLauncher.launch(Manifest.permission.RECORD_AUDIO) else startMonitor()
                },
                enabled = kinds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(tr("Arm")) }
        }
        ClipsCard(store, clipVersion) { clipVersion++ }
        if (showLegal) {
            AlertDialog(
                onDismissRequest = { showLegal = false },
                title = { Text(tr("Recording voices")) },
                text = {
                    Text(
                        tr("Recording the voices of other people may be restricted by law where you live. Use this only where it is allowed, such as in your own space, and tell the people who are there. Clips stay on this phone only; the last 20 are kept."),
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        prefs.edit().putBoolean("legal_ok", true).apply()
                        record = true
                        showLegal = false
                    }) { Text(tr("I understand")) }
                },
                dismissButton = {
                    TextButton(onClick = { showLegal = false }) { Text(tr("Cancel")) }
                },
            )
        }
        return
    }

    ArmedGuard(
        m, state, refresh,
        voiceOn = Kind.VOICE in kinds,
        record = record && Kind.VOICE in kinds,
        store = store,
        onClip = { clipVersion++ },
        onTick = { refresh = it },
        onDisarm = { m.disarm(); monitor = null },
    )
    ClipsCard(store, clipVersion) { clipVersion++ }
}

@Composable
private fun ArmedGuard(
    m: GuardMonitor,
    state: FieldwatchUi,
    refresh: Long,
    voiceOn: Boolean,
    record: Boolean,
    store: ClipStore,
    onClip: () -> Unit,
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

    LaunchedEffect(m, voiceOn, record) {
        if (voiceOn) withContext(Dispatchers.IO) { listenForVoices(m, record, store, onClip) }
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
        if (record) {
            Text(
                tr("Recording clips when a voice is heard. Only where the law allows it."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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


private const val MAX_CLIP_BYTES = 60 * VoiceDetector.SAMPLE_RATE * 2
private const val PRE_ROLL_FRAMES = 31

/**
 * Listens to the microphone, flags voices to the guard and, if asked, saves a clip around each
 * one. Runs until its coroutine is cancelled. Audio never leaves the phone.
 */
private suspend fun listenForVoices(
    m: GuardMonitor,
    record: Boolean,
    store: ClipStore,
    onClip: () -> Unit,
) {
    val minBuf = AudioRecord.getMinBufferSize(
        VoiceDetector.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
    )
    if (minBuf <= 0) return
    val rec = try {
        AudioRecord(
            MediaRecorder.AudioSource.MIC,
            VoiceDetector.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuf, 8192),
        )
    } catch (e: Exception) {
        return
    }
    if (rec.state != AudioRecord.STATE_INITIALIZED) {
        rec.release()
        return
    }
    val main = Handler(Looper.getMainLooper())
    val detector = VoiceDetector()
    val frame = ShortArray(VoiceDetector.FRAME_SAMPLES)
    val preRoll = ArrayDeque<ShortArray>()
    var writer: Wav.Writer? = null
    fun closeClip() {
        writer?.let {
            runCatching { it.close() }
            store.prune()
            main.post { onClip() }
        }
        writer = null
    }
    try {
        rec.startRecording()
        while (currentCoroutineContext().isActive) {
            var read = 0
            while (read < frame.size) {
                val r = rec.read(frame, read, frame.size - read)
                if (r <= 0) break
                read += r
            }
            if (read < frame.size) {
                kotlinx.coroutines.delay(20)
                continue
            }
            val now = SystemClock.elapsedRealtime()
            val was = detector.speaking
            val speaking = detector.process(frame, now)
            val armed = m.phase == GuardMonitor.Phase.ARMED
            if (speaking && !was && armed) main.post { m.onVoiceStart(SystemClock.elapsedRealtime()) }
            if (record && armed && speaking) {
                if (writer == null) {
                    writer = Wav.Writer(store.newFile(System.currentTimeMillis()))
                    preRoll.forEach { writer?.write(it) }
                    preRoll.clear()
                }
                writer?.write(frame)
                if ((writer?.dataBytes ?: 0) >= MAX_CLIP_BYTES) closeClip()
            } else {
                if (writer != null) closeClip()
                preRoll.addLast(frame.copyOf())
                while (preRoll.size > PRE_ROLL_FRAMES) preRoll.removeFirst()
            }
        }
    } finally {
        closeClip()
        runCatching { rec.stop() }
        rec.release()
    }
}

@Composable
private fun ClipsCard(store: ClipStore, version: Int, onChanged: () -> Unit) {
    val context = LocalContext.current
    val clips = remember(version) { store.list() }
    var playing by remember { mutableStateOf<String?>(null) }
    val player = remember { arrayOfNulls<MediaPlayer>(1) }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { player[0]?.release() }
            player[0] = null
        }
    }
    if (clips.isEmpty()) return
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    SectionCard("Saved clips") {
        Text(
            tr("Newest first. Only the last 20 are kept."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        clips.forEach { f ->
            val stamp = f.name.removePrefix("voice_").removeSuffix(".wav").toLongOrNull()
            val seconds = ((f.length() - 44).coerceAtLeast(0) / (VoiceDetector.SAMPLE_RATE * 2)).toInt()
            Text(
                (stamp?.let { fmt.format(Date(it)) } ?: f.name) + " · ${seconds}s",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldwatchActionButton(
                    onClick = {
                        runCatching { player[0]?.release() }
                        if (playing == f.name) {
                            playing = null
                            player[0] = null
                        } else {
                            val p = MediaPlayer()
                            runCatching {
                                p.setDataSource(f.absolutePath)
                                p.prepare()
                                p.setOnCompletionListener { playing = null }
                                p.start()
                                player[0] = p
                                playing = f.name
                            }.onFailure { p.release(); playing = null }
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text(tr(if (playing == f.name) "Stop" else "Play")) }
                FieldwatchActionButton(
                    onClick = {
                        runCatching {
                            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.files", f)
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "audio/wav"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text(tr("Share")) }
                FieldwatchActionButton(
                    onClick = {
                        if (playing == f.name) {
                            runCatching { player[0]?.release() }
                            player[0] = null
                            playing = null
                        }
                        f.delete()
                        onChanged()
                    },
                    modifier = Modifier.weight(1f),
                ) { Text(tr("Delete")) }
            }
        }
        FieldwatchActionButton(
            onClick = {
                runCatching { player[0]?.release() }
                player[0] = null
                playing = null
                store.deleteAll()
                onChanged()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(tr("Delete all clips")) }
    }
}
