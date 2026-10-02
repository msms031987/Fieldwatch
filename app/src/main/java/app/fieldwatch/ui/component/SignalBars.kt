package app.fieldwatch.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.fieldwatch.domain.Proximities
import app.fieldwatch.domain.Proximity
import app.fieldwatch.ui.i18n.tr

/** Four-step signal bars, like a phone's reception icon. */
@Composable
fun SignalBars(
    proximity: Proximity,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    width: Dp = 20.dp,
    height: Dp = 14.dp,
) {
    val off = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    Canvas(modifier.size(width, height)) {
        val gap = size.width * 0.08f
        val barW = (size.width - gap * 3) / 4f
        for (i in 0 until 4) {
            val h = size.height * (0.3f + 0.7f * (i + 1) / 4f)
            drawRoundRect(
                color = if (i < proximity.bars) color else off,
                topLeft = Offset(i * (barW + gap), size.height - h),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW * 0.25f),
            )
        }
    }
}

/** "How to read the signal": what the bands, the radar and Hunt mean, in plain words. */
@Composable
fun SignalHelpDialog(onDismiss: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("How to read the signal")) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    tr("A stronger signal usually means the device is closer. Walls, bodies and the device itself change it, so use it as a hint."),
                    style = MaterialTheme.typography.bodySmall,
                )
                Proximity.entries.forEach { p ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        SignalBars(p, Modifier.padding(top = 2.dp))
                        Column {
                            Text(tr(p.label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(tr(p.meaning), style = MaterialTheme.typography.bodySmall, color = muted)
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tr("The radar"), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        tr("The closer to the center, the stronger the signal. Where a dot sits around the circle does not show direction: the phone cannot tell which side a signal comes from."),
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tr("To find a device"), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        tr("Walk and watch whether the signal rises or falls. That is more reliable than any single reading. Open a radio and use Hunt."),
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                    )
                }
                Text(tr(Proximities.NOT_DISTANCE), style = MaterialTheme.typography.labelSmall, color = muted)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Got it")) } },
    )
}
