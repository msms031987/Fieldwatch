package app.fieldwatch.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.fieldwatch.domain.ViewMode
import app.fieldwatch.ui.i18n.tr
import app.fieldwatch.ui.theme.BissaBlue

private data class LiveTab(val label: String, val mode: ViewMode)

private val liveTabs = listOf(
    LiveTab("List", ViewMode.LIST),
    LiveTab("Radar", ViewMode.RADAR),
    LiveTab("Classes", ViewMode.BY_CLASS),
    LiveTab("Timeline", ViewMode.TIMELINE),
)

/**
 * Always-visible switcher for the four main Live views. "Hybrid + sparklines" is a list
 * variant, so it keeps the List tab lit; it is still chosen from the scan options panel.
 */
@Composable
fun LiveViewTabs(
    mode: ViewMode,
    onChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
    onHelp: (() -> Unit)? = null,
) {
    val accent = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier.fillMaxWidth()) {
        liveTabs.forEach { tab ->
            val selected = mode == tab.mode || (tab.mode == ViewMode.LIST && mode == ViewMode.HYBRID)
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clickable { onChange(tab.mode) }
                    .drawBehind {
                        if (selected) {
                            drawRect(
                                accent,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 2.dp.toPx()),
                                size = Size(size.width, 2.dp.toPx()),
                            )
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tr(tab.label).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 1.6.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) accent else muted,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        if (onHelp != null) {
            Box(
                Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .clickable(onClick = onHelp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.HelpOutline,
                    contentDescription = tr("How to read the signal"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
}

/** Pulsing status dot plus label: SCANNING, PAUSED or IDLE. */
@Composable
fun ScanPulse(scanning: Boolean, paused: Boolean) {
    val transition = rememberInfiniteTransition(label = "scanPulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanPulseAlpha",
    )
    val (label, color, beating) = when {
        paused -> Triple(tr("PAUSED"), MaterialTheme.colorScheme.primary, false)
        scanning -> Triple(tr("SCANNING"), BissaBlue, true)
        else -> Triple(tr("IDLE"), MaterialTheme.colorScheme.onSurfaceVariant, false)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .alpha(if (beating) pulse else 1f)
                .background(color, CircleShape),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.6.sp),
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
            softWrap = false,
        )
    }
}
