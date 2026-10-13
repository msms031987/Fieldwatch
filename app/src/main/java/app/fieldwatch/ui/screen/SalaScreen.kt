package app.fieldwatch.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fieldwatch.domain.RadioRole
import app.fieldwatch.domain.Sighting
import app.fieldwatch.domain.ViewMode
import app.fieldwatch.ui.FieldwatchUi
import app.fieldwatch.ui.FieldwatchViewModel
import app.fieldwatch.ui.component.color
import app.fieldwatch.ui.i18n.tr
import app.fieldwatch.ui.theme.BissaBlue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sala mode: a situation-room layout for Live. Role counters and a clock on top, then radar,
 * the radio list and an attention feed side by side on wide screens, or radar over list on a phone.
 */
@Composable
fun SalaPane(state: FieldwatchUi, vm: FieldwatchViewModel, onOpen: (Sighting) -> Unit) {
    val alertedKeys by vm.alertedKeys.collectAsStateWithLifecycle()
    val onAir = state.filtered.filter { !it.gone }
    val roleOf = { d: Sighting -> vm.deviceRole(d, alerted = d.key in alertedKeys) }
    val counts = RadioRole.entries.associateWith { role -> onAir.count { roleOf(it) == role } }
    val attention = state.filtered
        .filter { roleOf(it) == RadioRole.ATTENTION }
        .sortedByDescending { it.lastSeen }

    BoxWithConstraints(Modifier.fillMaxSize().padding(8.dp)) {
        val wide = maxWidth >= 720.dp
        val maxH = maxHeight
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SalaHeader(counts, onAir.size, state.displayPaused)
            if (wide) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HudPanel(tr("RADAR"), Modifier.weight(1f).fillMaxSize()) {
                        LivePane(state, vm, onOpen, forceMode = ViewMode.RADAR, banners = false)
                    }
                    HudPanel(tr("RADIOS"), Modifier.weight(1.15f).fillMaxSize()) {
                        LivePane(state, vm, onOpen, forceMode = ViewMode.LIST, banners = true)
                    }
                    HudPanel(tr("ATTENTION"), Modifier.weight(0.85f).fillMaxSize()) {
                        AttentionFeed(attention, vm, alertedKeys, onOpen)
                    }
                }
            } else {
                val radarHeight: Dp = minOf(280.dp, maxH * 0.38f)
                HudPanel(tr("RADAR"), Modifier.fillMaxWidth().height(radarHeight)) {
                    LivePane(state, vm, onOpen, forceMode = ViewMode.RADAR, banners = false)
                }
                HudPanel(tr("RADIOS"), Modifier.weight(1f).fillMaxWidth()) {
                    LivePane(state, vm, onOpen, forceMode = ViewMode.LIST, banners = true)
                }
            }
        }
    }
}

@Composable
private fun SalaHeader(counts: Map<RadioRole, Int>, total: Int, paused: Boolean) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val clock = remember(now / 1000) { SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(now)) }
    val gold = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                (tr("SALA") + if (paused) "  ·  " + tr("PAUSED") else ""),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = gold,
            )
            Text(
                "$total " + tr("on air").uppercase() + "   $clock",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                RadioRole.ATTENTION to "Attention",
                RadioRole.PRESENCE to "Presence",
                RadioRole.VEHICLE to "Vehicle",
                RadioRole.INFRA to "Infra",
                RadioRole.UNKNOWN to "Unknown",
            ).forEach { (role, label) ->
                val n = counts[role] ?: 0
                val hot = role == RadioRole.ATTENTION && n > 0
                Column(
                    Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (hot) role.color().copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            RoundedCornerShape(3.dp),
                        )
                        .background(
                            if (hot) role.color().copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(3.dp),
                        )
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "$n",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = role.color(),
                    )
                    Text(
                        tr(label).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Panel with a thin blue frame, gold corner brackets and a small caption. */
@Composable
private fun HudPanel(title: String, modifier: Modifier, content: @Composable () -> Unit) {
    val bracket = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
            .border(1.dp, BissaBlue.copy(alpha = 0.28f), RoundedCornerShape(2.dp))
            .drawBehind { drawBrackets(bracket) },
    ) {
        Text(
            title,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBrackets(color: Color) {
    val len = 14.dp.toPx()
    val w = 2.dp.toPx()
    val x1 = 0f
    val y1 = 0f
    val x2 = size.width
    val y2 = size.height
    // top-left
    drawLine(color, Offset(x1, y1), Offset(x1 + len, y1), w)
    drawLine(color, Offset(x1, y1), Offset(x1, y1 + len), w)
    // top-right
    drawLine(color, Offset(x2, y1), Offset(x2 - len, y1), w)
    drawLine(color, Offset(x2, y1), Offset(x2, y1 + len), w)
    // bottom-left
    drawLine(color, Offset(x1, y2), Offset(x1 + len, y2), w)
    drawLine(color, Offset(x1, y2), Offset(x1, y2 - len), w)
    // bottom-right
    drawLine(color, Offset(x2, y2), Offset(x2 - len, y2), w)
    drawLine(color, Offset(x2, y2), Offset(x2, y2 - len), w)
}

@Composable
private fun AttentionFeed(
    devices: List<Sighting>,
    vm: FieldwatchViewModel,
    alertedKeys: Set<String>,
    onOpen: (Sighting) -> Unit,
) {
    if (devices.isEmpty()) {
        Text(
            tr("Nothing needs attention."),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(devices, key = { it.key }) { device ->
            DeviceRow(
                device = device,
                vm = vm,
                sparklines = false,
                compact = true,
                onOpen = onOpen,
                showBar = false,
                alerted = device.key in alertedKeys,
            )
        }
    }
}
