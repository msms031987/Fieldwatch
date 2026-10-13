package app.fieldwatch.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.fieldwatch.ui.theme.GoldActive
import app.fieldwatch.ui.theme.LocalNightMode
import app.fieldwatch.ui.theme.nightIf

/** BISSA switch: square track and square thumb, gold when on. */
@Composable
fun FieldwatchSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val fill = spectreTileFill()
    val edge = spectreTileEdge()
    val active = GoldActive.nightIf(LocalNightMode.current)
    val shape = RoundedCornerShape(2.dp)
    val thumbX by animateDpAsState(if (checked) 20.dp else 2.dp, label = "switchThumb")
    val toggle = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        )
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .requiredSize(width = 36.dp, height = 20.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(if (checked) active.copy(alpha = 0.14f) else fill)
            .border(1.dp, if (checked) active.copy(alpha = 0.6f) else edge, shape)
            .then(toggle),
    ) {
        Box(
            Modifier
                .offset(x = thumbX, y = 3.dp)
                .size(12.dp)
                .background(
                    if (checked) active else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    RoundedCornerShape(1.dp),
                ),
        )
    }
}
