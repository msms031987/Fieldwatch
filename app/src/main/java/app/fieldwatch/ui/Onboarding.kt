package app.fieldwatch.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.fieldwatch.domain.FieldwatchDisclaimer
import app.fieldwatch.domain.RadioRole
import app.fieldwatch.ui.component.bissaOpsecWordmark
import app.fieldwatch.ui.component.color
import app.fieldwatch.ui.i18n.tr

/** Faint 40 dp grid, the BISSA app background motif. */
private fun Modifier.bissaGrid(line: Color): Modifier = drawBehind {
    val step = 40.dp.toPx()
    var x = 0f
    while (x < size.width) {
        drawLine(line, Offset(x, 0f), Offset(x, size.height), 1f)
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(line, Offset(0f, y), Offset(size.width, y), 1f)
        y += step
    }
}

@Composable
private fun Wordmark(size: Int = 14) {
    Text(
        bissaOpsecWordmark(
            accent = MaterialTheme.colorScheme.primary,
            base = MaterialTheme.colorScheme.onSurface,
            muted = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        style = MaterialTheme.typography.titleMedium.copy(fontSize = size.sp),
        fontWeight = FontWeight.Bold,
        letterSpacing = (size / 4.5f).sp,
        maxLines = 1,
    )
}

/** EN | ES switch used on the intro and in the top bar. */
@Composable
fun LanguageChip(language: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val gold = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier
            .border(BorderStroke(1.dp, gold.copy(alpha = 0.3f)), RoundedCornerShape(2.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf("en" to "EN", "es" to "ES").forEach { (code, label) ->
            val on = language == code
            Text(
                label,
                modifier = Modifier
                    .background(if (on) gold.copy(alpha = 0.16f) else Color.Transparent)
                    .clickable { onChange(code) }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.2.sp),
                fontWeight = FontWeight.Bold,
                color = if (on) gold else muted,
            )
        }
    }
}

@Composable
private fun RingIcon(icon: ImageVector) {
    val gold = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .size(96.dp)
            .background(gold.copy(alpha = 0.04f), CircleShape)
            .border(1.dp, gold.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = gold, modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun GoldButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(3.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        ),
        modifier = Modifier.fillMaxWidth().height(48.dp),
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            fontWeight = FontWeight.Bold,
        )
    }
}

private data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val highlight: String,
    val body: String,
    val legend: Boolean = false,
)

private val slides = listOf(
    OnboardingSlide(
        Icons.Outlined.Visibility,
        "Hear what your phone hears",
        "hears",
        "BISSA OpSec only listens. It lists the Wi-Fi access points and Bluetooth LE advertisers around you. No account, no server, nothing is transmitted.",
    ),
    OnboardingSlide(
        Icons.Outlined.Palette,
        "Colors carry meaning",
        "meaning",
        "One color per role, so the list reads at a glance.",
        legend = true,
    ),
    OnboardingSlide(
        Icons.Outlined.Dashboard,
        "Read it your way",
        "way",
        "Switch between List, Radar, Classes and Timeline. Use Filters to hide noise and Signatures to name what you find. On a tablet, turn on Sala mode in Settings.",
    ),
    OnboardingSlide(
        Icons.Outlined.Security,
        "Your data stays here",
        "stays",
        "Android needs Location, Nearby devices and Notifications to scan. Everything stays on this phone until you share it.",
    ),
)

private val legendRows = listOf(
    Triple(RadioRole.ATTENTION, "Attention", "Needs a look: alerts and flagged equipment."),
    Triple(RadioRole.PRESENCE, "Presence", "Carried by people: phones, wearables, trackers, headphones."),
    Triple(RadioRole.VEHICLE, "Vehicle", "Cars and their hotspots."),
    Triple(RadioRole.INFRA, "Infrastructure", "Routers, mesh, home IoT, access control, speakers."),
    Triple(RadioRole.UNKNOWN, "Unknown or fading", "No signature, or not heard for a while."),
)

@Composable
fun OnboardingScreen(language: String, onLanguage: (String) -> Unit, onDone: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val slide = slides[page]
    val gold = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val last = page == slides.lastIndex
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .bissaGrid(gold.copy(alpha = 0.04f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp, vertical = 16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Wordmark(13)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LanguageChip(language, onLanguage)
                if (!last) {
                    Text(
                        tr("Skip").uppercase(),
                        modifier = Modifier.clickable(onClick = onDone).padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = muted,
                    )
                }
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(16.dp))
            RingIcon(slide.icon)
            Spacer(Modifier.height(26.dp))
            val title = tr(slide.title)
            val hl = tr(slide.highlight)
            Text(
                buildAnnotatedString {
                    val i = title.indexOf(hl, ignoreCase = true)
                    if (i < 0) {
                        append(title)
                    } else {
                        append(title.substring(0, i))
                        withStyle(SpanStyle(color = gold)) { append(title.substring(i, i + hl.length)) }
                        append(title.substring(i + hl.length))
                    }
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                tr(slide.body),
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (slide.legend) {
                Spacer(Modifier.height(18.dp))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    legendRows.forEach { (role, label, desc) ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                Modifier
                                    .padding(top = 4.dp)
                                    .size(10.dp)
                                    .background(role.color(), CircleShape),
                            )
                            Column {
                                Text(tr(label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(tr(desc), style = MaterialTheme.typography.bodySmall, color = muted)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Row(
            Modifier.fillMaxWidth().padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slides.indices.forEach { i ->
                Box(
                    Modifier
                        .height(5.dp)
                        .width(if (i == page) 18.dp else 5.dp)
                        .background(
                            if (i == page) gold else muted.copy(alpha = 0.3f),
                            RoundedCornerShape(3.dp),
                        ),
                )
            }
        }
        GoldButton(if (last) tr("Continue") else tr("Next")) {
            if (last) onDone() else page += 1
        }
    }
}


@Composable
fun TermsScreen(language: String, onLanguage: (String) -> Unit, onAccept: () -> Unit) {
    val gold = MaterialTheme.colorScheme.primary
    val ink = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    var agreed by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .bissaGrid(gold.copy(alpha = 0.04f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Wordmark(13)
            LanguageChip(language, onLanguage)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                tr("Terms and license").uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp,
                color = gold,
            )
            Text(
                tr("Before you start"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ink,
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(3.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    tr("Disclaimer").uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = gold,
                )
                listOf(
                    FieldwatchDisclaimer.HOBBY,
                    FieldwatchDisclaimer.HYPOTHESES,
                    FieldwatchDisclaimer.LIABILITY,
                ).forEach { Text(tr(it), style = MaterialTheme.typography.bodySmall, color = ink) }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(3.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    FieldwatchDisclaimer.LICENSE_TITLE.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = gold,
                )
                if (language == "es") {
                    Text(
                        tr("The license text is kept in English."),
                        style = MaterialTheme.typography.labelSmall,
                        color = muted,
                    )
                }
                Text(FieldwatchDisclaimer.LICENSE_BODY, style = MaterialTheme.typography.bodySmall, color = muted)
            }
            Text(tr(FieldwatchDisclaimer.ACCEPT), style = MaterialTheme.typography.bodySmall, color = muted)
            Row(
                Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (agreed) gold.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        RoundedCornerShape(3.dp),
                    )
                    .background(if (agreed) gold.copy(alpha = 0.05f) else Color.Transparent, RoundedCornerShape(3.dp))
                    .toggleable(value = agreed, onValueChange = { agreed = it }, role = Role.Checkbox)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(18.dp)
                        .background(if (agreed) gold else Color.Transparent, RoundedCornerShape(3.dp))
                        .border(1.5.dp, if (agreed) gold else muted, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (agreed) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                Text(tr("I have read this and I agree"), style = MaterialTheme.typography.bodyMedium, color = ink)
            }
        }
        Spacer(Modifier.height(12.dp))
        GoldButton(tr("Continue"), enabled = agreed, onClick = onAccept)
    }
}

@Composable
fun PermissionScreen(language: String, onLanguage: (String) -> Unit, onRequest: () -> Unit) {
    val gold = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .bissaGrid(gold.copy(alpha = 0.04f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp, vertical = 16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Wordmark(13)
            LanguageChip(language, onLanguage)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            RingIcon(Icons.Outlined.Security)
            Spacer(Modifier.height(24.dp))
            Text(
                tr("BISSA OpSec needs the radios"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                tr("Location, nearby Wi-Fi, Bluetooth scan, and notifications let BISSA OpSec passively watch advertised networks and BLE devices. Nothing is transmitted."),
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
                textAlign = TextAlign.Center,
            )
        }
        GoldButton(tr("Grant permissions"), onClick = onRequest)
    }
}
