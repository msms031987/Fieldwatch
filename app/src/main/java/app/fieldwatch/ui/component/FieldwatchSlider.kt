package app.fieldwatch.ui.component

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.fieldwatch.ui.theme.LocalNightMode
import app.fieldwatch.ui.theme.GoldActive
import app.fieldwatch.ui.theme.nightIf

@Composable
fun FieldwatchSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = SliderDefaults.colors(
            thumbColor = GoldActive.nightIf(LocalNightMode.current),
            activeTrackColor = GoldActive.nightIf(LocalNightMode.current),
            activeTickColor = GoldActive.nightIf(LocalNightMode.current),
        ),
    )
}
