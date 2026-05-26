package site.hanzhe.wuchang_liuyao.feature.time

import site.hanzhe.wuchang_liuyao.domain.time.CalendarSummary
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTime
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.domain.time.LunarDate
import site.hanzhe.wuchang_liuyao.domain.time.LunarMonthOption
import site.hanzhe.wuchang_liuyao.domain.time.SolarDateTime
import site.hanzhe.wuchang_liuyao.domain.time.currentGregorianDivinationTime

internal val HourOptions = (0..23).toList()
internal val MinuteOptions = (0..59).toList()
internal val HeavenlyStemOptions = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
internal val EarthlyBranchOptions = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

internal sealed interface StartupFailure {
    val message: String

    data object InvalidSystemTime : StartupFailure {
        override val message: String = "当前系统时间不正确"
    }

    data object CalendarDataLoadFailed : StartupFailure {
        override val message: String = "日历数据加载失败"
    }
}

internal data class TimeSelectionHostUiState(
    val isLoading: Boolean = true,
    val startupFailure: StartupFailure? = null,
    val transientMessage: String? = null,
    val shouldCloseTimeSelection: Boolean = false,
    val calendarSummary: CalendarSummary? = null,
    val divinationTime: DivinationTime = currentGregorianDivinationTime(),
    val confirmedTimeSelectionUiState: TimeSelectionUiState? = null,
    val timeSelectionUiState: TimeSelectionUiState? = null
)

internal enum class TimePickerType(val title: String) {
    SolarYear("选择年份"),
    SolarMonth("选择月份"),
    SolarDay("选择日期"),
    LunarYear("选择年份"),
    LunarMonth("选择月份"),
    LunarDay("选择日期"),
    Hour("选择小时"),
    Minute("选择分钟")
}

internal data class PickerOption(
    val id: String,
    val label: String
)

internal data class TimePickerDialogState(
    val title: String,
    val selectedOptionId: String,
    val options: List<PickerOption>
)

internal data class TimeSelectionUiState(
    val selectedType: DivinationTimeType,
    val solarDateTime: SolarDateTime,
    val lunarDate: LunarDate,
    val ganzhiText: String,
    val ganzhiSelectionState: GanzhiSelectionState = GanzhiSelectionState(),
    val solarYearOptions: List<Int>,
    val solarMonthOptions: List<Int>,
    val solarDayOptions: List<Int>,
    val lunarYearOptions: List<Int>,
    val lunarMonthOptions: List<LunarMonthOption>,
    val lunarDayOptions: List<Int>,
    val activePicker: TimePickerType? = null
)

internal fun buildTimePickerDialogState(uiState: TimeSelectionUiState): TimePickerDialogState? {
    return when (uiState.activePicker) {
        TimePickerType.SolarYear -> TimePickerDialogState(
            title = TimePickerType.SolarYear.title,
            selectedOptionId = uiState.solarDateTime.year.toString(),
            options = uiState.solarYearOptions.map { PickerOption(id = it.toString(), label = it.toString()) }
        )

        TimePickerType.SolarMonth -> TimePickerDialogState(
            title = TimePickerType.SolarMonth.title,
            selectedOptionId = uiState.solarDateTime.month.toString(),
            options = uiState.solarMonthOptions.map { PickerOption(id = it.toString(), label = it.toString()) }
        )

        TimePickerType.SolarDay -> TimePickerDialogState(
            title = TimePickerType.SolarDay.title,
            selectedOptionId = uiState.solarDateTime.day.toString(),
            options = uiState.solarDayOptions.map { PickerOption(id = it.toString(), label = it.toString()) }
        )

        TimePickerType.LunarYear -> TimePickerDialogState(
            title = TimePickerType.LunarYear.title,
            selectedOptionId = uiState.lunarDate.year.toString(),
            options = uiState.lunarYearOptions.map { PickerOption(id = it.toString(), label = it.toString()) }
        )

        TimePickerType.LunarMonth -> TimePickerDialogState(
            title = TimePickerType.LunarMonth.title,
            selectedOptionId = LunarMonthOption(
                month = uiState.lunarDate.month,
                isLeapMonth = uiState.lunarDate.isLeapMonth
            ).id,
            options = uiState.lunarMonthOptions.map { PickerOption(id = it.id, label = it.label) }
        )

        TimePickerType.LunarDay -> TimePickerDialogState(
            title = TimePickerType.LunarDay.title,
            selectedOptionId = uiState.lunarDate.day.toString(),
            options = uiState.lunarDayOptions.map { PickerOption(id = it.toString(), label = it.toString()) }
        )

        TimePickerType.Hour -> TimePickerDialogState(
            title = TimePickerType.Hour.title,
            selectedOptionId = uiState.solarDateTime.hour.toString(),
            options = HourOptions.map { PickerOption(id = it.toString(), label = it.toString()) }
        )

        TimePickerType.Minute -> TimePickerDialogState(
            title = TimePickerType.Minute.title,
            selectedOptionId = uiState.solarDateTime.minute.toString(),
            options = MinuteOptions.map { PickerOption(id = it.toString(), label = it.toString()) }
        )

        null -> null
    }
}

internal enum class GanzhiFieldType {
    YearStem,
    YearBranch,
    MonthStem,
    MonthBranch,
    DayStem,
    DayBranch
}

internal data class GanzhiSelectionState(
    val yearStem: String? = null,
    val yearBranch: String? = null,
    val monthStem: String? = null,
    val monthBranch: String? = null,
    val dayStem: String? = null,
    val dayBranch: String? = null,
    val activeField: GanzhiFieldType = GanzhiFieldType.YearStem,
    val deleteVisibleField: GanzhiFieldType? = null
) {
    val isRequiredFieldsComplete: Boolean
        get() = !monthBranch.isNullOrBlank() &&
            !dayStem.isNullOrBlank() &&
            !dayBranch.isNullOrBlank()
}

internal fun GanzhiSelectionState.toGanzhiText(): String {
    return buildList {
        buildGanzhiPillarText(yearStem, yearBranch, "年")?.let(::add)
        buildGanzhiPillarText(monthStem, monthBranch, "月")?.let(::add)
        buildGanzhiPillarText(dayStem, dayBranch, "日")?.let(::add)
    }.joinToString(separator = "    ")
}

internal fun GanzhiSelectionState.optionsFor(fieldType: GanzhiFieldType): List<String> {
    return when (fieldType) {
        GanzhiFieldType.YearStem -> filterCompatibleStems(yearBranch)
        GanzhiFieldType.YearBranch -> filterCompatibleBranches(yearStem)
        GanzhiFieldType.MonthStem -> filterCompatibleStems(monthBranch)
        GanzhiFieldType.MonthBranch -> filterCompatibleBranches(monthStem)
        GanzhiFieldType.DayStem -> filterCompatibleStems(dayBranch)
        GanzhiFieldType.DayBranch -> filterCompatibleBranches(dayStem)
    }
}

internal fun GanzhiSelectionState.select(fieldType: GanzhiFieldType, value: String): GanzhiSelectionState {
    return when (fieldType) {
        GanzhiFieldType.YearStem -> copy(
            yearStem = value,
            activeField = GanzhiFieldType.YearBranch,
            deleteVisibleField = null
        )

        GanzhiFieldType.YearBranch -> copy(
            yearBranch = value,
            activeField = GanzhiFieldType.MonthStem,
            deleteVisibleField = null
        )

        GanzhiFieldType.MonthStem -> copy(
            monthStem = value,
            activeField = GanzhiFieldType.MonthBranch,
            deleteVisibleField = null
        )

        GanzhiFieldType.MonthBranch -> copy(
            monthBranch = value,
            activeField = GanzhiFieldType.DayStem,
            deleteVisibleField = null
        )

        GanzhiFieldType.DayStem -> copy(
            dayStem = value,
            activeField = GanzhiFieldType.DayBranch,
            deleteVisibleField = null
        )

        GanzhiFieldType.DayBranch -> copy(
            dayBranch = value,
            deleteVisibleField = null
        )
    }
}

internal fun GanzhiSelectionState.valueOf(fieldType: GanzhiFieldType): String? {
    return when (fieldType) {
        GanzhiFieldType.YearStem -> yearStem
        GanzhiFieldType.YearBranch -> yearBranch
        GanzhiFieldType.MonthStem -> monthStem
        GanzhiFieldType.MonthBranch -> monthBranch
        GanzhiFieldType.DayStem -> dayStem
        GanzhiFieldType.DayBranch -> dayBranch
    }
}

internal fun GanzhiSelectionState.activateField(fieldType: GanzhiFieldType): GanzhiSelectionState {
    val currentValue = valueOf(fieldType)
    return if (activeField == fieldType && !currentValue.isNullOrBlank() && deleteVisibleField == null) {
        copy(deleteVisibleField = fieldType)
    } else {
        copy(
            activeField = fieldType,
            deleteVisibleField = null
        )
    }
}

internal fun GanzhiSelectionState.dismissDeleteField(): GanzhiSelectionState {
    return if (deleteVisibleField == null) {
        this
    } else {
        copy(deleteVisibleField = null)
    }
}

internal fun GanzhiSelectionState.clearField(fieldType: GanzhiFieldType): GanzhiSelectionState {
    return when (fieldType) {
        GanzhiFieldType.YearStem -> copy(yearStem = null, activeField = fieldType, deleteVisibleField = null)
        GanzhiFieldType.YearBranch -> copy(yearBranch = null, activeField = fieldType, deleteVisibleField = null)
        GanzhiFieldType.MonthStem -> copy(monthStem = null, activeField = fieldType, deleteVisibleField = null)
        GanzhiFieldType.MonthBranch -> copy(monthBranch = null, activeField = fieldType, deleteVisibleField = null)
        GanzhiFieldType.DayStem -> copy(dayStem = null, activeField = fieldType, deleteVisibleField = null)
        GanzhiFieldType.DayBranch -> copy(dayBranch = null, activeField = fieldType, deleteVisibleField = null)
    }
}

internal fun GanzhiSelectionState.reset(): GanzhiSelectionState {
    return GanzhiSelectionState()
}

internal fun buildGanzhiSelectionStateFromText(ganzhiText: String): GanzhiSelectionState {
    val yearPillar = parseGanzhiPillar(ganzhiText, '年')
    val monthPillar = parseGanzhiPillar(ganzhiText, '月')
    val dayPillar = parseGanzhiPillar(ganzhiText, '日')
    return GanzhiSelectionState(
        yearStem = yearPillar?.first,
        yearBranch = yearPillar?.second,
        monthStem = monthPillar?.first,
        monthBranch = monthPillar?.second,
        dayStem = dayPillar?.first,
        dayBranch = dayPillar?.second
    )
}

private fun buildGanzhiPillarText(
    stem: String?,
    branch: String?,
    suffix: String
): String? {
    val resolvedStem = stem.orEmpty()
    val resolvedBranch = branch.orEmpty()
    return if (resolvedStem.isNotBlank() || resolvedBranch.isNotBlank()) {
        "$resolvedStem$resolvedBranch$suffix"
    } else {
        null
    }
}

private fun parseGanzhiPillar(ganzhiText: String, suffix: Char): Pair<String, String>? {
    val markerIndex = ganzhiText.indexOf(suffix)
    if (markerIndex < 2) {
        return null
    }
    val branch = ganzhiText[markerIndex - 1].toString()
    val stem = ganzhiText[markerIndex - 2].toString()
    return if (
        stem in HeavenlyStemOptions &&
        branch in EarthlyBranchOptions &&
        isGanzhiPairCompatible(stem, branch)
    ) {
        stem to branch
    } else {
        null
    }
}

private fun filterCompatibleStems(branch: String?): List<String> {
    if (branch.isNullOrBlank()) {
        return HeavenlyStemOptions
    }
    return HeavenlyStemOptions.filter { stem -> isGanzhiPairCompatible(stem, branch) }
}

private fun filterCompatibleBranches(stem: String?): List<String> {
    if (stem.isNullOrBlank()) {
        return EarthlyBranchOptions
    }
    return EarthlyBranchOptions.filter { branch -> isGanzhiPairCompatible(stem, branch) }
}

private fun isGanzhiPairCompatible(stem: String, branch: String): Boolean {
    val stemIndex = HeavenlyStemOptions.indexOf(stem)
    val branchIndex = EarthlyBranchOptions.indexOf(branch)
    if (stemIndex < 0 || branchIndex < 0) {
        return false
    }
    // 天干地支同阴同阳才能组成合法干支
    return stemIndex % 2 == branchIndex % 2
}
