package app.fieldwatch.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.fieldwatch.domain.Glint
import app.fieldwatch.domain.GlintDetector
import app.fieldwatch.domain.GlintTracker
import app.fieldwatch.domain.LensScanner
import app.fieldwatch.domain.MagneticLevel
import app.fieldwatch.domain.MagneticMeter
import app.fieldwatch.domain.SignatureClass
import app.fieldwatch.ui.FieldwatchUi
import app.fieldwatch.ui.FieldwatchViewModel
import app.fieldwatch.ui.NestedTabInsets
import app.fieldwatch.ui.NestedTopBar
import app.fieldwatch.ui.component.FieldwatchActionButton
import app.fieldwatch.ui.component.FieldwatchFilterChip
import app.fieldwatch.ui.component.SectionCard
import app.fieldwatch.ui.i18n.tr
import app.fieldwatch.ui.theme.BissaBlue
import app.fieldwatch.ui.theme.SignalRed
import app.fieldwatch.ui.theme.Slate
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlinx.coroutines.delay

/**
 * Sweep: a prototype toolbox for checking a room. Every tool here narrows where to look with
 * your own eyes. None of them proves a room is clean, and the screen says so.
 */
@Composable
fun SweepScreen(
    state: FieldwatchUi,
    vm: FieldwatchViewModel,
    onBack: () -> Unit,
    onOpenLive: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = NestedTabInsets,
        topBar = {
            NestedTopBar(
                title = "Sweep",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                tr("Prototype. These tools help you decide where to look. They cannot prove a room is clean, and a camera that records to a card and never connects to anything can pass every one of them. Nothing from the camera or sensors is saved or sent."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RadiosCard(state, vm, onOpenLive)
            SectionCard("Lenses and LEDs") { LensFinder() }
            SectionCard("Magnetic field") { MagneticCard() }
            SectionCard("Checklist") { SweepChecklist() }
        }
    }
}

@Composable
private fun RadiosCard(state: FieldwatchUi, vm: FieldwatchViewModel, onOpenLive: () -> Unit) {
    val cameraLike = state.filtered.count { d ->
        !d.gone && d.fleetIds.any { id ->
            vm.fleetKind(id).let { it == SignatureClass.CAMERA || it == SignatureClass.SURVEILLANCE }
        }
    }
    SectionCard("Radios") {
        Text(
            if (cameraLike == 0) {
                tr("No camera-like radios heard right now.")
            } else {
                "$cameraLike " + tr("camera-like radios heard right now.")
            },
            style = MaterialTheme.typography.titleSmall,
            color = if (cameraLike > 0) SignalRed else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            tr("These are Wi-Fi and Bluetooth signatures of known camera makers. Cameras that never join a network do not appear here."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FieldwatchActionButton(onClick = onOpenLive, modifier = Modifier.fillMaxWidth()) {
            Text(tr("Open Live"))
        }
    }
}

private enum class LensMode { LENS, IR }

@Composable
private fun LensFinder() {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    var running by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf(LensMode.LENS) }
    var irFront by rememberSaveable { mutableStateOf(true) }

    Text(
        if (mode == LensMode.LENS) {
            tr("Lens finder: turn the room lights off. The torch blinks on and off: a lens throws the torch back, so its glint shows only while the torch is on. Lit LEDs, lamps and screens shine with the torch off too and are ignored. Sweep slowly across walls, shelves, outlets, clocks and smoke detectors.")
        } else {
            tr("IR check: in a dark room, some cameras show infrared LEDs as a faint bright dot. First test the camera: point a TV remote at the lens and press a button. If you see a light on screen, that camera sees infrared. Many cameras filter it out, so seeing nothing means little.")
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldwatchFilterChip(
            selected = mode == LensMode.LENS,
            onClick = { mode = LensMode.LENS },
            label = { Text(tr("Lens finder")) },
        )
        FieldwatchFilterChip(
            selected = mode == LensMode.IR,
            onClick = { mode = LensMode.IR },
            label = { Text(tr("IR check")) },
        )
    }
    if (mode == LensMode.IR) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldwatchFilterChip(
                selected = irFront,
                onClick = { irFront = true },
                label = { Text(tr("Front camera")) },
            )
            FieldwatchFilterChip(
                selected = !irFront,
                onClick = { irFront = false },
                label = { Text(tr("Back camera")) },
            )
        }
    }
    when {
        !granted -> FieldwatchActionButton(
            onClick = { launcher.launch(Manifest.permission.CAMERA) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(tr("Allow camera")) }
        !running -> FieldwatchActionButton(
            onClick = { running = true },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(tr("Start")) }
        else -> {
            AmbientLightNote(maxLux = if (mode == LensMode.LENS) 40f else 15f)
            CameraPane(mode, irFront)
            FieldwatchActionButton(
                onClick = { running = false },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(tr("Stop")) }
        }
    }
}

/** Says whether the room is dark enough. Bright rooms add reflections of other lights. */
@Composable
private fun AmbientLightNote(maxLux: Float) {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val sensor = remember { manager?.getDefaultSensor(Sensor.TYPE_LIGHT) }
    var lux by remember { mutableStateOf<Float?>(null) }
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                lux = event.values[0]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (manager != null && sensor != null) {
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose { manager?.unregisterListener(listener) }
    }
    val value = lux ?: return
    val bright = value > maxLux
    Text(
        if (bright) {
            tr("The room is bright.") + " (${value.toInt()} lux) " + tr("Turn off the lights for fewer false alarms.")
        } else {
            tr("Dark enough.") + " (${value.toInt()} lux)"
        },
        style = MaterialTheme.typography.labelMedium,
        color = if (bright) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CameraPane(mode: LensMode, irFront: Boolean) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val front = mode == LensMode.IR && irFront
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FIT_CENTER }
    }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var hasTorch by remember { mutableStateOf(false) }
    var candidates by remember { mutableStateOf<List<GlintTracker.Candidate>>(emptyList()) }
    var failure by remember { mutableStateOf<String?>(null) }
    var dismissed by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val scanner = remember(mode, front) { LensScanner(SystemClock.elapsedRealtime()) }
    val plainTracker = remember(mode, front) { GlintTracker() }
    val useScanner = remember { AtomicBoolean(false) }
    useScanner.set(mode == LensMode.LENS && hasTorch)

    DisposableEffect(front) {
        val executor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        future.addListener({
            try {
                val p = future.get()
                provider = p
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { image ->
                    val frame = readFrame(image) ?: return@setAnalyzer
                    val found = if (useScanner.get()) {
                        scanner.onFrame(frame.glints, SystemClock.elapsedRealtime())
                    } else {
                        plainTracker.update(frame.glints)
                    }
                    val shown = found.map { toDisplay(it, frame.rotation, front) }
                    mainExecutor.execute { candidates = shown }
                }
                val selector = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                p.unbindAll()
                val bound = p.bindToLifecycle(owner, selector, preview, analysis)
                camera = bound
                hasTorch = bound.cameraInfo.hasFlashUnit()
                failure = null
            } catch (e: Exception) {
                failure = e.message ?: e.javaClass.simpleName
            }
        }, mainExecutor)
        onDispose {
            provider?.unbindAll()
            executor.shutdown()
            camera = null
            hasTorch = false
        }
    }

    // Lens finder: a darker exposure so only strong reflections stand out. Otherwise default.
    LaunchedEffect(camera, mode) {
        val cam = camera ?: return@LaunchedEffect
        val state = cam.cameraInfo.exposureState
        if (state.isExposureCompensationSupported) {
            val step = state.exposureCompensationStep.toFloat()
            val range = state.exposureCompensationRange
            val index = if (mode == LensMode.LENS && step > 0f) {
                Math.round(-2f / step).coerceIn(range.lower, range.upper)
            } else {
                0
            }
            cam.cameraControl.setExposureCompensationIndex(index)
        }
    }

    // Torch blinks in the Lens finder; stays off in the IR check.
    LaunchedEffect(camera, mode, hasTorch) {
        val cam = camera ?: return@LaunchedEffect
        if (!hasTorch) return@LaunchedEffect
        if (mode != LensMode.LENS) {
            cam.cameraControl.enableTorch(false)
            return@LaunchedEffect
        }
        var last: Boolean? = null
        while (true) {
            val on = scanner.torchShouldBeOn(SystemClock.elapsedRealtime())
            if (on != last) {
                cam.cameraControl.enableTorch(on)
                last = on
            }
            delay(40)
        }
    }

    val visible = candidates.filter { c ->
        dismissed.none { abs(it.x - c.x) + abs(it.y - c.y) < 0.06f }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(visible) {
                    detectTapGestures { tap ->
                        val x = tap.x / size.width
                        val y = tap.y / size.height
                        visible
                            .filter { abs(it.x - x) + abs(it.y - y) < 0.12f }
                            .minByOrNull { abs(it.x - x) + abs(it.y - y) }
                            ?.let { dismissed = dismissed + Offset(it.x, it.y) }
                    }
                },
        ) {
            visible.forEach { c ->
                val center = Offset(c.x * size.width, c.y * size.height)
                val color = if (c.confirmed) Color(0xFFFFAD32) else Slate
                drawCircle(color, radius = if (c.confirmed) 26f else 16f, center = center, style = Stroke(if (c.confirmed) 4f else 2f))
                if (c.confirmed) {
                    drawLine(color, Offset(center.x - 40f, center.y), Offset(center.x - 30f, center.y), 3f)
                    drawLine(color, Offset(center.x + 30f, center.y), Offset(center.x + 40f, center.y), 3f)
                }
            }
        }
    }
    val confirmed = visible.count { it.confirmed }
    Text(
        when {
            failure != null -> tr("The camera could not start.") + " " + failure
            confirmed > 0 -> "$confirmed " + tr("steady bright spot(s). Look at the place with your own eyes: is there a small lens or LED?")
            visible.isNotEmpty() -> tr("A flicker. Hold steady on it.")
            else -> tr("Nothing yet. Move slowly. Reflections of lamps in glass can look like this too.")
        },
        style = MaterialTheme.typography.titleSmall,
        color = if (confirmed > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
    if (mode == LensMode.LENS && camera != null && !hasTorch) {
        Text(
            tr("This camera has no torch, so the blink test is off. Results will include lit LEDs and lamps."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Text(
        tr("Tap a marked spot to dismiss it while you hold still."),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (dismissed.isNotEmpty()) {
        FieldwatchActionButton(onClick = { dismissed = emptyList() }, modifier = Modifier.fillMaxWidth()) {
            Text(tr("Show dismissed spots"))
        }
    }
}

private class Frame(val glints: List<Glint>, val rotation: Int)

/** Reads one camera frame into glints (still in sensor orientation) and closes it. */
private fun readFrame(image: ImageProxy): Frame? = try {
    image.use { img ->
        val plane = img.planes[0]
        val buffer = plane.buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        val pooled = GlintDetector.maxPool(data, img.width, img.height, plane.rowStride, plane.pixelStride, block = 4)
        Frame(GlintDetector.detect(pooled.data, pooled.width, pooled.height), img.imageInfo.rotationDegrees)
    }
} catch (e: Exception) {
    null
}

/** Maps a candidate from sensor orientation to the way the preview is shown. */
private fun toDisplay(c: GlintTracker.Candidate, rotation: Int, front: Boolean): GlintTracker.Candidate {
    var x: Float
    val y: Float
    when (rotation) {
        90 -> { x = 1f - c.y; y = c.x }
        180 -> { x = 1f - c.x; y = 1f - c.y }
        270 -> { x = c.y; y = 1f - c.x }
        else -> { x = c.x; y = c.y }
    }
    if (front) x = 1f - x
    return GlintTracker.Candidate(x, y, c.hits, c.confirmed)
}

@Composable
private fun MagneticCard() {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val sensor = remember { manager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    if (manager == null || sensor == null) {
        Text(
            tr("This phone has no magnetic field sensor."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val meter = remember { MagneticMeter() }
    var refresh by remember { mutableIntStateOf(0) }
    var running by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(running) {
        var lastUi = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                meter.add(event.values[0], event.values[1], event.values[2], event.timestamp)
                if (event.timestamp - lastUi > 100_000_000L) {
                    lastUi = event.timestamp
                    refresh++
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (running) manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { manager.unregisterListener(listener) }
    }

    Text(
        tr("Measures magnets, metal and electric currents near the phone. It does not detect radio waves, so it cannot find a transmitter. Set a baseline in a clear spot, then move slowly along furniture and walls and watch for a change."),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (!running) {
        FieldwatchActionButton(onClick = { running = true }, modifier = Modifier.fillMaxWidth()) {
            Text(tr("Start"))
        }
        return
    }
    if (refresh < 0) return
    val level = meter.level()
    val delta = meter.delta()
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "%.1f".format(meter.latest),
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
            fontWeight = FontWeight.Bold,
        )
        Text("µT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (delta != null) {
            Text(
                (if (delta >= 0) "+" else "") + "%.1f".format(delta),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                color = when (level) {
                    MagneticLevel.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
                    MagneticLevel.ELEVATED -> MaterialTheme.colorScheme.primary
                    MagneticLevel.STRONG -> SignalRed
                },
            )
        }
    }
    Text(
        when {
            delta == null -> tr("No baseline yet. Tap Set baseline in a clear spot.")
            level == MagneticLevel.NORMAL -> tr("Normal: close to the baseline.")
            level == MagneticLevel.ELEVATED -> tr("Changed: something magnetic or metal is near.")
            else -> tr("Strong change: a magnet, speaker, motor or large piece of metal is very close.")
        },
        style = MaterialTheme.typography.titleSmall,
    )
    if (meter.fluctuating()) {
        Text(
            tr("The reading is moving a lot: metal being moved, a motor or AC wiring nearby."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    val history = meter.history()
    val lineColor = BissaBlue
    val gridColor = MaterialTheme.colorScheme.outline
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(64.dp),
    ) {
        drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height), 1f)
        if (history.size > 2) {
            val lo = (history.min() - 2f)
            val hi = (history.max() + 2f).coerceAtLeast(lo + 5f)
            val path = Path()
            history.forEachIndexed { i, v ->
                val x = size.width * i / (history.size - 1)
                val y = size.height * (1f - (v - lo) / (hi - lo))
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = Stroke(3f))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldwatchActionButton(
            onClick = { meter.setBaselineFromRecent(); refresh++ },
            modifier = Modifier.weight(1f),
        ) { Text(tr("Set baseline")) }
        FieldwatchActionButton(
            onClick = { running = false },
            modifier = Modifier.weight(1f),
        ) { Text(tr("Stop")) }
    }
}

private val checklistItems = listOf(
    "Smoke detectors, clocks, USB chargers and plugs: look for a tiny lens or LED.",
    "Vents, shelves, plants and decorations that face the bed or the desk.",
    "TV, set-top box and speakers: look for small holes or lenses.",
    "Mirrors: touch the glass with a fingertip. If your finger touches its own reflection with no gap, it may be a two-way mirror.",
    "Lights out: run the Lens finder, then the IR check, slowly around the room.",
    "Radios: open Live and look for red or camera-like radios.",
    "Magnetic field: set a baseline, then sweep furniture and walls.",
    "Anything plugged in with no clear purpose, or that is new since you last looked.",
)

@Composable
private fun SweepChecklist() {
    val done = remember { mutableStateOf(setOf<Int>()) }
    checklistItems.forEachIndexed { i, text ->
        val on = i in done.value
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { done.value = if (on) done.value - i else done.value + i },
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(checked = on, onCheckedChange = null, modifier = Modifier.padding(top = 2.dp, end = 10.dp))
            Text(
                tr(text),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = if (on) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
