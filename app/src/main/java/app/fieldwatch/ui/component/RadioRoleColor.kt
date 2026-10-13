package app.fieldwatch.ui.component

import androidx.compose.ui.graphics.Color
import app.fieldwatch.domain.RadioRole
import app.fieldwatch.ui.theme.BissaBlue
import app.fieldwatch.ui.theme.Gold
import app.fieldwatch.ui.theme.GoldSoft
import app.fieldwatch.ui.theme.SignalRed
import app.fieldwatch.ui.theme.Slate

fun RadioRole.color(): Color = when (this) {
    RadioRole.ATTENTION -> SignalRed
    RadioRole.PRESENCE -> Gold
    RadioRole.VEHICLE -> GoldSoft
    RadioRole.INFRA -> BissaBlue
    RadioRole.UNKNOWN -> Slate
}
