package site.hanzhe.wuchang_liuyao.data.settings

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

internal object AppFontScale {
    const val Min = 0.70f
    const val Max = 1.30f
    const val Step = 0.05f
    const val Default = 1.00f
    const val DiscreteSteps = 11

    fun normalize(scale: Float): Float {
        val normalized = (Min + ((scale - Min) / Step).roundToInt() * Step)
            .coerceIn(Min, Max)
        return (normalized * 100).roundToInt() / 100f
    }

    fun formatSummary(scale: Float): String {
        return if (abs(scale - Default) < 0.001f) {
            "标准大小"
        } else {
            String.format(Locale.US, "x%.2f倍", normalize(scale))
        }
    }

    fun fromLegacyKey(key: String?): Float {
        return when (key) {
            "very_small" -> 0.74f
            "small" -> 0.87f
            "standard" -> Default
            "large" -> 1.13f
            "very_large" -> 1.26f
            else -> Default
        }
    }
}
