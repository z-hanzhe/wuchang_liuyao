package site.hanzhe.wuchang_liuyao.navigation

import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationDateInfo
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationLine
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationRequest
import site.hanzhe.wuchang_liuyao.domain.divination.GanzhiPillar
import site.hanzhe.wuchang_liuyao.feature.home.DivinationMethod
import site.hanzhe.wuchang_liuyao.feature.home.HexagramNameSelectionState
import site.hanzhe.wuchang_liuyao.feature.home.HomeUiState
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.domain.time.buildHourGanzhi
import site.hanzhe.wuchang_liuyao.domain.time.formatGanzhiDivinationTime
import site.hanzhe.wuchang_liuyao.domain.time.formatLunarDivinationTime
import site.hanzhe.wuchang_liuyao.domain.time.formatSolarDivinationTime
import site.hanzhe.wuchang_liuyao.feature.time.TimeSelectionHostUiState
import site.hanzhe.wuchang_liuyao.feature.time.TimeSelectionUiState
import site.hanzhe.wuchang_liuyao.feature.time.toGanzhiText
import kotlin.random.Random

private const val GanzhiTimePlaceholder = "用户使用干支起卦"

internal sealed interface DivinationRequestBuildResult {
    data class Success(val request: DivinationRequest) : DivinationRequestBuildResult
    data class Failure(val message: String) : DivinationRequestBuildResult
}

internal fun buildDivinationRequest(
    homeUiState: HomeUiState,
    timeUiState: TimeSelectionHostUiState,
    changeDayPillarAt23: Boolean
): DivinationRequestBuildResult {
    val lines = when (homeUiState.selectedMethod) {
        DivinationMethod.YAO_NAME -> homeUiState.selectedYaoValues.map { value ->
            toDivinationLine(value)
                ?: return DivinationRequestBuildResult.Failure("请先完整选择六爻")
        }

        DivinationMethod.HEXAGRAM_NAME -> buildHexagramNameLines(
            selection = homeUiState.hexagramNameSelection
        ) ?: return DivinationRequestBuildResult.Failure("请先选择本卦的上卦和下卦")

        DivinationMethod.COIN -> homeUiState.selectedCoinValues.map { value ->
            when (value) {
                "背 正 正" -> DivinationLine.SHAO_YANG
                "背 背 正" -> DivinationLine.SHAO_YIN
                "背 背 背" -> DivinationLine.LAO_YANG
                "正 正 正" -> DivinationLine.LAO_YIN
                else -> return DivinationRequestBuildResult.Failure("请先完整选择六爻")
            }
        }

        DivinationMethod.POINT_SELECT -> {
            if (homeUiState.pointSelectionLines.size != 6) {
                return DivinationRequestBuildResult.Failure("点选起卦数据不完整，无法排盘")
            }
            homeUiState.pointSelectionLines.map { lineState ->
                when {
                    lineState.isYang && lineState.isMoving -> DivinationLine.LAO_YANG
                    lineState.isYang -> DivinationLine.SHAO_YANG
                    lineState.isMoving -> DivinationLine.LAO_YIN
                    else -> DivinationLine.SHAO_YIN
                }
            }
        }

        DivinationMethod.MANUAL -> homeUiState.onlineShakeValues.map { value ->
            toDivinationLine(value)
                ?: return DivinationRequestBuildResult.Failure("请先完成六爻摇卦")
        }

        DivinationMethod.AUTO -> List(6) {
            when (Random.nextInt(4)) {
                0 -> DivinationLine.SHAO_YANG
                1 -> DivinationLine.SHAO_YIN
                2 -> DivinationLine.LAO_YANG
                else -> DivinationLine.LAO_YIN
            }
        }
    }
    val confirmedTimeState = timeUiState.confirmedTimeSelectionUiState
        ?: return DivinationRequestBuildResult.Failure("时间数据尚未准备完成")
    val dateInfo = buildDateInfo(
        confirmedTimeState = confirmedTimeState,
        changeDayPillarAt23 = changeDayPillarAt23
    ) ?: return DivinationRequestBuildResult.Failure("干支时间信息不完整，无法排盘")
    return DivinationRequestBuildResult.Success(
        request = DivinationRequest(
            question = homeUiState.question,
            methodLabel = homeUiState.selectedMethod.label,
            dateInfo = dateInfo,
            linesTopDown = lines
        )
    )
}

/** 将本卦和可选变卦的上下卦转换为上爻到初爻的六爻。 */
private fun buildHexagramNameLines(
    selection: HexagramNameSelectionState
): List<DivinationLine>? {
    val baseUpper = selection.baseUpper ?: return null
    val baseLower = selection.baseLower ?: return null
    val changedUpper = selection.changedUpper ?: baseUpper
    val changedLower = selection.changedLower ?: baseLower
    val baseLinesTopDown = baseUpper.linesTopDown + baseLower.linesTopDown
    val changedLinesTopDown = changedUpper.linesTopDown + changedLower.linesTopDown

    return baseLinesTopDown.zip(changedLinesTopDown).map { (baseLine, changedLine) ->
        when {
            baseLine == '1' && changedLine == '0' -> DivinationLine.LAO_YANG
            baseLine == '0' && changedLine == '1' -> DivinationLine.LAO_YIN
            baseLine == '1' -> DivinationLine.SHAO_YANG
            else -> DivinationLine.SHAO_YIN
        }
    }
}

private fun toDivinationLine(value: String): DivinationLine? {
    return when (value) {
        "少阳" -> DivinationLine.SHAO_YANG
        "少阴" -> DivinationLine.SHAO_YIN
        "老阳" -> DivinationLine.LAO_YANG
        "老阴" -> DivinationLine.LAO_YIN
        else -> null
    }
}

private fun buildDateInfo(
    confirmedTimeState: TimeSelectionUiState,
    changeDayPillarAt23: Boolean
): DivinationDateInfo? {
    return when (confirmedTimeState.selectedType) {
        DivinationTimeType.GREGORIAN -> buildDateInfoFromGregorian(confirmedTimeState, changeDayPillarAt23)
        DivinationTimeType.LUNAR -> buildDateInfoFromLunar(confirmedTimeState, changeDayPillarAt23)
        DivinationTimeType.GANZHI -> buildDateInfoFromGanzhi(confirmedTimeState)
    }
}

private fun buildDateInfoFromGregorian(
    confirmedTimeState: TimeSelectionUiState,
    changeDayPillarAt23: Boolean
): DivinationDateInfo {
    val solarDateTime = confirmedTimeState.solarDateTime
    val baseSolar = Solar.fromYmdHms(
        solarDateTime.year,
        solarDateTime.month,
        solarDateTime.day,
        solarDateTime.hour,
        solarDateTime.minute,
        0
    )
    val useNextDayGanzhi = shouldUseNextDayGanzhi(changeDayPillarAt23, solarDateTime.hour)
    val resolvedLunar = if (useNextDayGanzhi) {
        baseSolar.next(1).lunar
    } else {
        baseSolar.lunar
    }
    return buildDateInfoFromResolvedLunar(
        timeType = confirmedTimeState.selectedType,
        solarText = formatSolarDivinationTime(solarDateTime),
        lunarText = formatLunarDivinationTime(
            lunarDate = confirmedTimeState.lunarDate,
            hour = solarDateTime.hour,
            minute = solarDateTime.minute
        ),
        lunar = resolvedLunar,
        termText = baseSolar.lunar.jieQi.ifBlank { null },
        selectedHour = solarDateTime.hour,
        useNextDayGanzhi = useNextDayGanzhi
    )
}

private fun buildDateInfoFromLunar(
    confirmedTimeState: TimeSelectionUiState,
    changeDayPillarAt23: Boolean
): DivinationDateInfo {
    val lunarDate = confirmedTimeState.lunarDate
    val hour = confirmedTimeState.solarDateTime.hour
    val minute = confirmedTimeState.solarDateTime.minute
    val baseSolar = Lunar.fromYmdHms(
        lunarDate.year,
        lunarDate.month.toSignedLunarMonth(lunarDate.isLeapMonth),
        lunarDate.day,
        hour,
        minute,
        0
    ).solar
    val useNextDayGanzhi = shouldUseNextDayGanzhi(changeDayPillarAt23, hour)
    val resolvedSolar = if (useNextDayGanzhi) baseSolar.next(1) else baseSolar
    val resolvedLunar = resolvedSolar.lunar
    val solar = baseSolar
    return buildDateInfoFromResolvedLunar(
        timeType = confirmedTimeState.selectedType,
        solarText = formatSolarDivinationTime(
            confirmedTimeState.solarDateTime.copy(
                year = solar.year,
                month = solar.month,
                day = solar.day
            )
        ),
        lunarText = formatLunarDivinationTime(
            lunarDate = lunarDate,
            hour = hour,
            minute = minute
        ),
        lunar = resolvedLunar,
        termText = baseSolar.lunar.jieQi.ifBlank { null },
        selectedHour = hour,
        useNextDayGanzhi = useNextDayGanzhi
    )
}

private fun buildDateInfoFromGanzhi(
    confirmedTimeState: TimeSelectionUiState
): DivinationDateInfo? {
    val selectionState = confirmedTimeState.ganzhiSelectionState
    if (
        selectionState.monthBranch.isNullOrBlank() ||
        selectionState.dayStem.isNullOrBlank() ||
        selectionState.dayBranch.isNullOrBlank()
    ) {
        return null
    }
    val ganzhiText = selectionState.toGanzhiText()
    return DivinationDateInfo(
        displayText = formatGanzhiDivinationTime(ganzhiText),
        timeType = confirmedTimeState.selectedType,
        solarText = GanzhiTimePlaceholder,
        lunarText = GanzhiTimePlaceholder,
        ganzhiText = formatGanzhiDivinationTime(ganzhiText),
        termText = "",
        year = GanzhiPillar(
            heavenlyStem = selectionState.yearStem,
            earthlyBranch = selectionState.yearBranch
        ),
        month = GanzhiPillar(
            heavenlyStem = selectionState.monthStem,
            earthlyBranch = selectionState.monthBranch
        ),
        day = GanzhiPillar(
            heavenlyStem = selectionState.dayStem,
            earthlyBranch = selectionState.dayBranch
        ),
        hour = GanzhiPillar()
    )
}

private fun buildDateInfoFromResolvedLunar(
    timeType: DivinationTimeType,
    solarText: String,
    lunarText: String,
    lunar: Lunar,
    termText: String?,
    selectedHour: Int,
    useNextDayGanzhi: Boolean
): DivinationDateInfo {
    val dayPillar = GanzhiPillar(
        heavenlyStem = lunar.dayGan,
        earthlyBranch = lunar.dayZhi
    )
    val hourPillar = if (useNextDayGanzhi) {
        buildHourPillar(
            dayStem = requireNotNull(dayPillar.heavenlyStem) { "日干不能为空" },
            hour = selectedHour
        )
    } else {
        GanzhiPillar(
            heavenlyStem = lunar.timeGan,
            earthlyBranch = lunar.timeZhi
        )
    }
    val dateInfo = DivinationDateInfo(
        displayText = if (timeType == DivinationTimeType.GREGORIAN) solarText else lunarText,
        timeType = timeType,
        solarText = solarText,
        lunarText = lunarText,
        ganzhiText = "",
        termText = termText.orEmpty(),
        year = GanzhiPillar(
            heavenlyStem = lunar.yearGanExact,
            earthlyBranch = lunar.yearZhiExact
        ),
        month = GanzhiPillar(
            heavenlyStem = lunar.monthGanExact,
            earthlyBranch = lunar.monthZhiExact
        ),
        day = dayPillar,
        hour = hourPillar
    )
    return dateInfo.copy(
        ganzhiText = formatGanzhiDivinationTime(dateInfo.toGanzhiDisplayText())
    )
}

private fun shouldUseNextDayGanzhi(changeDayPillarAt23: Boolean, hour: Int): Boolean {
    return changeDayPillarAt23 && hour >= 23
}

private fun buildHourPillar(dayStem: String, hour: Int): GanzhiPillar {
    val ganzhi = buildHourGanzhi(dayStem, hour)
    return GanzhiPillar(
        heavenlyStem = ganzhi.take(1),
        earthlyBranch = ganzhi.takeLast(1)
    )
}

private fun DivinationDateInfo.toGanzhiDisplayText(): String {
    return buildList {
        year.text.takeIf { it.isNotBlank() }?.let { add("${it}年") }
        month.text.takeIf { it.isNotBlank() }?.let { add("${it}月") }
        day.text.takeIf { it.isNotBlank() }?.let { add("${it}日") }
        hour.text.takeIf { it.isNotBlank() }?.let { add("${it}时") }
    }.joinToString(separator = "    ")
}

private fun Int.toSignedLunarMonth(isLeapMonth: Boolean): Int {
    return if (isLeapMonth) -this else this
}
