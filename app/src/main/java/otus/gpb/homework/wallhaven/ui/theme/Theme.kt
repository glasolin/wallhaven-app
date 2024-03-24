package otus.gpb.homework.wallhaven.ui.theme

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ShortText
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Grid3x3
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Upcoming
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Colors {
    companion object {
        val Blue10 = Color(0xFF001F28)
        val Blue20 = Color(0xFF003544)
        val Blue30 = Color(0xFF004D61)
        val Blue40 = Color(0xFF006780)
        val Blue80 = Color(0xFF5DD5FC)
        val Blue90 = Color(0xFFB8EAFF)
        val DarkGreen10 = Color(0xFF0D1F12)
        val DarkGreen20 = Color(0xFF223526)
        val DarkGreen30 = Color(0xFF394B3C)
        val DarkGreen40 = Color(0xFF4F6352)
        val DarkGreen80 = Color(0xFFB7CCB8)
        val DarkGreen90 = Color(0xFFD3E8D3)
        val DarkGreenGray10 = Color(0xFF1A1C1A)
        val DarkGreenGray20 = Color(0xFF2F312E)
        val DarkGreenGray90 = Color(0xFFE2E3DE)
        val DarkGreenGray95 = Color(0xFFF0F1EC)
        val DarkGreenGray99 = Color(0xFFFBFDF7)
        val DarkPurpleGray10 = Color(0xFF201A1B)
        val DarkPurpleGray20 = Color(0xFF362F30)
        val DarkPurpleGray90 = Color(0xFFECDFE0)
        val DarkPurpleGray95 = Color(0xFFFAEEEF)
        val DarkPurpleGray99 = Color(0xFFFCFCFC)
        val Green10 = Color(0xFF00210B)
        val Green20 = Color(0xFF003919)
        val Green30 = Color(0xFF005227)
        val Green40 = Color(0xFF006D36)
        val Green80 = Color(0xFF0EE37C)
        val Green90 = Color(0xFF5AFF9D)
        val GreenGray30 = Color(0xFF414941)
        val GreenGray50 = Color(0xFF727971)
        val GreenGray60 = Color(0xFF8B938A)
        val GreenGray80 = Color(0xFFC1C9BF)
        val GreenGray90 = Color(0xFFDDE5DB)
        val Orange10 = Color(0xFF380D00)
        val Orange20 = Color(0xFF5B1A00)
        val Orange30 = Color(0xFF812800)
        val Orange40 = Color(0xFFA23F16)
        val Orange80 = Color(0xFFFFB59B)
        val Orange90 = Color(0xFFFFDBCF)
        val Purple10 = Color(0xFF36003C)
        val Purple20 = Color(0xFF560A5D)
        val Purple30 = Color(0xFF702776)
        val Purple40 = Color(0xFF8B418F)
        val Purple80 = Color(0xFFFFA9FE)
        val Purple90 = Color(0xFFFFD6FA)
        val PurpleGray30 = Color(0xFF4D444C)
        val PurpleGray50 = Color(0xFF7F747C)
        val PurpleGray60 = Color(0xFF998D96)
        val PurpleGray80 = Color(0xFFD0C3CC)
        val PurpleGray90 = Color(0xFFEDDEE8)
        val Red10 = Color(0xFF410002)
        val Red20 = Color(0xFF690005)
        val Red30 = Color(0xFF93000A)
        val Red40 = Color(0xFFBA1A1A)
        val Red80 = Color(0xFFFFB4AB)
        val Red90 = Color(0xFFFFDAD6)
        val Teal10 = Color(0xFF001F26)
        val Teal20 = Color(0xFF02363F)
        val Teal30 = Color(0xFF214D56)
        val Teal40 = Color(0xFF3A656F)
        val Teal80 = Color(0xFFA2CED9)
        val Teal90 = Color(0xFFBEEAF6)
        val White = Color.White
        val Black = Color.Black
    }
}

object AppIcons {
    val Add = Icons.Rounded.Add
    val ArrowBack = Icons.AutoMirrored.Rounded.ArrowBack
    val Bookmark = Icons.Rounded.Bookmark
    val BookmarkBorder = Icons.Rounded.BookmarkBorder
    val Bookmarks = Icons.Rounded.Bookmarks
    val BookmarksBorder = Icons.Outlined.Bookmarks
    val Check = Icons.Rounded.Check
    val Close = Icons.Rounded.Close
    val Grid3x3 = Icons.Rounded.Grid3x3
    val MoreVert = Icons.Default.MoreVert
    val Person = Icons.Rounded.Person
    val Search = Icons.Rounded.Search
    var Site = Icons.Rounded.Public
    val Settings = Icons.Rounded.Settings
    val SortAsc = Icons.Filled.ArrowDownward
    val SortDesc = Icons.Filled.ArrowUpward
    val ClearCache = Icons.Filled.Delete
    val Reload = Icons.Rounded.Refresh
    val Filter = Icons.Filled.FilterAlt
    val ShortText = Icons.AutoMirrored.Rounded.ShortText
    val Upcoming = Icons.Rounded.Upcoming
    val UpcomingBorder = Icons.Outlined.Upcoming
    val ViewDay = Icons.Rounded.ViewDay
}

@VisibleForTesting
val LightDefaultColorScheme = lightColorScheme(
    primary = Colors.Purple40,
    onPrimary = Color.White,
    primaryContainer = Colors.Purple90,
    onPrimaryContainer = Colors.Purple10,
    secondary = Colors.Orange40,
    onSecondary = Colors.White,
    secondaryContainer = Colors.Orange90,
    onSecondaryContainer = Colors.Orange10,
    tertiary = Colors.Blue40,
    onTertiary = Colors.White,
    tertiaryContainer = Colors.Blue90,
    onTertiaryContainer = Colors.Blue10,
    error = Colors.Red40,
    onError = Color.White,
    errorContainer = Colors.Red90,
    onErrorContainer = Colors.Red10,
    background = Colors.DarkPurpleGray99,
    onBackground = Colors.DarkPurpleGray10,
    surface = Colors.DarkPurpleGray99,
    onSurface = Colors.DarkPurpleGray10,
    surfaceVariant = Colors.PurpleGray90,
    onSurfaceVariant = Colors.PurpleGray30,
    inverseSurface = Colors.DarkPurpleGray20,
    inverseOnSurface = Colors.DarkPurpleGray95,
    outline = Colors.PurpleGray50,
)

@VisibleForTesting
val DarkDefaultColorScheme = darkColorScheme(
    primary = Colors.Purple80,
    onPrimary = Colors.Purple20,
    primaryContainer = Colors.Purple30,
    onPrimaryContainer = Colors.Purple90,
    secondary = Colors.Orange80,
    onSecondary = Colors.Orange20,
    secondaryContainer = Colors.Orange30,
    onSecondaryContainer = Colors.Orange90,
    tertiary = Colors.Blue80,
    onTertiary = Colors.Blue20,
    tertiaryContainer = Colors.Blue30,
    onTertiaryContainer = Colors.Blue90,
    error = Colors.Red80,
    onError = Colors.Red20,
    errorContainer = Colors.Red30,
    onErrorContainer = Colors.Red90,
    background = Colors.DarkPurpleGray10,
    onBackground = Colors.DarkPurpleGray90,
    surface = Colors.DarkPurpleGray10,
    onSurface = Colors.DarkPurpleGray90,
    surfaceVariant = Colors.PurpleGray30,
    onSurfaceVariant = Colors.PurpleGray80,
    inverseSurface = Colors.DarkPurpleGray90,
    inverseOnSurface = Colors.DarkPurpleGray10,
    outline = Colors.PurpleGray60,
)

internal val Typography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.None,
        ),
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Default text style
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // Used for Button
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
    ),
    // Used for Navigation items
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
    ),
    // Used for Tag
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
    ),
)

data class GradientColors(
    val top: Color = Color.Unspecified,
    val bottom: Color = Color.Unspecified,
    val container: Color = Color.Unspecified,
)

data class BackgroundTheme(
    val color: Color = Color.Unspecified,
    val tonalElevation: Dp = Dp.Unspecified,
)

open class PieChartColors(
    val colorFree:Color=Color.Unspecified,
    val colorCache:Color=Color.Unspecified,
    val colorFavorites:Color=Color.Unspecified,
)
object StoragePieChartDarkColors:PieChartColors(
    Colors.Blue40,
    Colors.Purple80,
    Colors.DarkPurpleGray90,
)

object StoragePieChartLightColors:PieChartColors(
    Colors.Blue40,
    Colors.Purple80,
    Colors.Orange40,
)

val LocalStoragePieChartColors = compositionLocalOf { PieChartColors() }

/**
 * A composition local for [BackgroundTheme].
 */
val LocalBackgroundTheme = staticCompositionLocalOf { BackgroundTheme() }


/**
 * A composition local for [GradientColors].
 */
val LocalGradientColors = staticCompositionLocalOf { GradientColors() }


/**
 * Light Android gradient colors
 */
val LightAndroidGradientColors = GradientColors(container = Colors.DarkGreenGray95)

/**
 * Dark Android gradient colors
 */
val DarkAndroidGradientColors = GradientColors(container = Colors.Black)

/**
 * Light Android background theme
 */
val LightAndroidBackgroundTheme = BackgroundTheme(color = Colors.DarkGreenGray95)

/**
 * Dark Android background theme
 */
val DarkAndroidBackgroundTheme = BackgroundTheme(color = Color.Black)

data class TintTheme(
    val iconTint: Color = Color.Unspecified,
)

/**
 * A composition local for [TintTheme].
 */
val LocalTintTheme = staticCompositionLocalOf { TintTheme() }

/**
 * Now in Android theme.
 *
 * @param darkTheme Whether the theme should use a dark color scheme (follows system by default).
 * @param androidTheme Whether the theme should use the Android theme color scheme instead of the
 *        default theme.
 * @param disableDynamicTheming If `true`, disables the use of dynamic theming, even when it is
 *        supported. This parameter has no effect if [androidTheme] is `true`.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Color scheme
    val colorScheme = if (darkTheme) DarkDefaultColorScheme else LightDefaultColorScheme

    // Gradient colors
    val emptyGradientColors = GradientColors(container = colorScheme.surfaceColorAtElevation(2.dp))
    val defaultGradientColors = GradientColors(
        top = colorScheme.inverseOnSurface,
        bottom = colorScheme.primaryContainer,
        container = colorScheme.surface,
    )
    val gradientColors = defaultGradientColors

    // Background theme
    val defaultBackgroundTheme = BackgroundTheme(
        color = colorScheme.surface,
        tonalElevation = 2.dp,
    )
    val backgroundTheme = defaultBackgroundTheme

    val storagePieChartTheme = if (darkTheme) StoragePieChartLightColors else StoragePieChartDarkColors

    val tintTheme = TintTheme()

    // Composition locals
    CompositionLocalProvider(
        LocalGradientColors provides gradientColors,
        LocalBackgroundTheme provides backgroundTheme,
        LocalTintTheme provides tintTheme,
        LocalStoragePieChartColors provides storagePieChartTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

