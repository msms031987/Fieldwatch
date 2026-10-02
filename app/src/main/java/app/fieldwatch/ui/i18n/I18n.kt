package app.fieldwatch.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** Current UI language code: "en" or "es". */
val LocalLanguage = staticCompositionLocalOf { "en" }

/**
 * Translate an English UI string. Looks the text up in [EsStrings]; anything without an entry
 * is shown unchanged, so a screen can be translated a few strings at a time.
 */
@Composable
@ReadOnlyComposable
fun tr(en: String): String =
    if (LocalLanguage.current == "es") EsStrings.map[en] ?: en else en
