package site.hanzhe.wuchang_liuyao.domain.divination

import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType

internal enum class DivinationLine(val code: Int) {
    SHAO_YANG(1),
    SHAO_YIN(2),
    LAO_YANG(3),
    LAO_YIN(4);

    val isYang: Boolean
        get() = code % 2 == 1

    val isChanging: Boolean
        get() = code > 2

    val baseMark: Char
        get() = if (isYang) '1' else '0'

    val changedMark: Char
        get() = if (code == SHAO_YANG.code || code == LAO_YIN.code) '1' else '0'

    val changeSymbol: String
        get() = when (this) {
            LAO_YANG -> "o"
            LAO_YIN -> "x"
            else -> ""
        }
}

internal data class GanzhiPillar(
    val heavenlyStem: String? = null,
    val earthlyBranch: String? = null
) {
    val text: String
        get() = heavenlyStem.orEmpty() + earthlyBranch.orEmpty()
}

internal data class DivinationDateInfo(
    val displayText: String,
    val timeType: DivinationTimeType,
    val solarText: String,
    val lunarText: String,
    val ganzhiText: String,
    val termText: String = "",
    val year: GanzhiPillar,
    val month: GanzhiPillar,
    val day: GanzhiPillar,
    val hour: GanzhiPillar
)

internal data class DivinationSpirit(
    val name: String,
    val value: String
)

internal data class DivinationRequest(
    val question: String,
    val methodLabel: String,
    val dateInfo: DivinationDateInfo,
    val linesTopDown: List<DivinationLine>
)

internal data class DivinationTableRow(
    val sixGod: String,
    val hiddenRelative: String,
    val hiddenStem: String,
    val hiddenBranch: String,
    val hiddenElement: String,
    val baseRelative: String,
    val baseStem: String,
    val baseBranch: String,
    val baseElement: String,
    val baseLineSymbol: String,
    val shiYingMark: String,
    val changingSymbol: String,
    val changedLineSymbol: String,
    val changedRelative: String,
    val changedStem: String,
    val changedBranch: String,
    val changedElement: String
)

internal data class DivinationResult(
    val question: String,
    val methodLabel: String,
    val dateInfo: DivinationDateInfo,
    val spirits: List<DivinationSpirit>,
    val dayXunKong: String,
    val baseHexagramName: String,
    val palaceName: String,
    val hexagramType: String?,
    val changedHexagramName: String?,
    val changedPalaceName: String?,
    val changedHexagramType: String?,
    val rowsTopDown: List<DivinationTableRow>
)
