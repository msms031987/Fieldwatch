package app.fieldwatch.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * "BISSA OPSEC" wordmark: the A of BISSA and the O and S of OPSEC are drawn in [accent],
 * the rest in [base]. [suffix] (for example "  ·  PAUSED") is drawn in [muted].
 */
fun bissaOpsecWordmark(
    accent: Color,
    base: Color,
    muted: Color,
    suffix: String = "",
): AnnotatedString = buildAnnotatedString {
    fun word(text: String, accentAt: Set<Int>) {
        text.forEachIndexed { i, ch ->
            withStyle(SpanStyle(color = if (i in accentAt) accent else base)) { append(ch) }
        }
    }
    word("BISSA", setOf(4))
    withStyle(SpanStyle(color = base)) { append(" ") }
    word("OPSEC", setOf(0, 2))
    if (suffix.isNotEmpty()) withStyle(SpanStyle(color = muted)) { append(suffix) }
}
