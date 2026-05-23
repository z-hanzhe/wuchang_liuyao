package site.hanzhe.wuchang_liuyao.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density

private const val AdaptiveReferenceShortestWidthDp = 360f
private const val AdaptiveMinScale = 0.92f
private const val AdaptiveMaxScale = 1.18f

val LocalAppFontScale = staticCompositionLocalOf { 1f }

private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = Color.White,
    secondary = AppPrimarySurface,
    tertiary = AppPrimaryDark,
    error = AppHighlightRed,
    onError = Color.White,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = AppPrimaryContainer,
    onSurfaceVariant = AppTextSecondary,
    outline = AppBorder
)

@Composable
fun Wuchang_liuyaoTheme(
    appFontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val resources = context.resources
    val baseDensityValue = resources.displayMetrics.density
    val baseFontScaleValue = configuration.fontScale
    val uiScale = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        // 以 360dp 短边为基准做全局缩放，限制极端设备放大或缩小幅度
        val shortestWidthDp = minOf(
            configuration.screenWidthDp,
            configuration.screenHeightDp
        ).toFloat()
        (shortestWidthDp / AdaptiveReferenceShortestWidthDp)
            .coerceIn(AdaptiveMinScale, AdaptiveMaxScale)
    }
    val scaledDensity = remember(
        baseDensityValue,
        baseFontScaleValue,
        uiScale,
        appFontScale
    ) {
        Density(
            density = baseDensityValue * uiScale,
            fontScale = baseFontScaleValue * appFontScale
        )
    }

    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides scaledDensity,
        LocalAppFontScale provides appFontScale
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
