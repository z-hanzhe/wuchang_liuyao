package site.hanzhe.wuchang_liuyao.domain.time

import java.util.Calendar
import java.util.Date

internal enum class DivinationTimeType(val label: String) {
    GREGORIAN("公历"),
    LUNAR("农历"),
    GANZHI("干支")
}

internal data class SolarDateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
)

internal data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean
)

internal data class LunarMonthOption(
    val month: Int,
    val isLeapMonth: Boolean
) {
    val label: String
        get() = formatSelectableLunarMonth(month, isLeapMonth)

    val id: String
        get() = "$month:${if (isLeapMonth) 1 else 0}"
}

internal data class DivinationTime(
    val type: DivinationTimeType,
    val solarDateTime: SolarDateTime,
    val value: String
)

internal data class CalendarSummary(
    val lunarYearText: String,
    val lunarMonthText: String,
    val lunarDayText: String,
    val termText: String,
    val ganzhiSummaryText: String
)

internal fun currentGregorianDivinationTime(currentDate: Date = Date()): DivinationTime {
    val calendar = Calendar.getInstance().apply { time = currentDate }
    val solarDateTime = SolarDateTime(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH),
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE)
    )
    return DivinationTime(
        type = DivinationTimeType.GREGORIAN,
        solarDateTime = solarDateTime,
        value = formatSolarDivinationTime(solarDateTime)
    )
}

internal fun buildDivinationTime(
    type: DivinationTimeType,
    solarDateTime: SolarDateTime,
    lunarDate: LunarDate,
    ganzhiText: String
): DivinationTime {
    val value = when (type) {
        DivinationTimeType.GREGORIAN -> formatSolarDivinationTime(solarDateTime)
        DivinationTimeType.LUNAR -> formatLunarDivinationTime(lunarDate, solarDateTime.hour, solarDateTime.minute)
        DivinationTimeType.GANZHI -> formatGanzhiDivinationTime(ganzhiText)
    }
    return DivinationTime(
        type = type,
        solarDateTime = solarDateTime,
        value = value
    )
}

internal fun formatSolarDivinationTime(solarDateTime: SolarDateTime): String {
    return "公历 ${solarDateTime.year}年${solarDateTime.month}月${solarDateTime.day}日 " +
        "${solarDateTime.hour}时${solarDateTime.minute}分"
}

internal fun formatLunarDivinationTime(
    lunarDate: LunarDate,
    hour: Int,
    minute: Int
): String {
    val monthText = formatDisplayLunarMonth(lunarDate.month, lunarDate.isLeapMonth)
    return "农历 ${lunarDate.year}年${monthText}月${lunarDate.day}日 ${hour}时${minute}分"
}

internal fun formatGanzhiDivinationTime(ganzhiText: String): String {
    return "干支 ${ganzhiText.replace("    ", " ")}"
}

internal fun formatDisplayLunarMonth(month: Int, isLeapMonth: Boolean): String {
    return if (isLeapMonth) "闰$month" else month.toString()
}

internal fun formatSelectableLunarMonth(month: Int, isLeapMonth: Boolean): String {
    return if (isLeapMonth) "$month（闰）" else month.toString()
}

internal fun buildHourGanzhi(dayGan: String, hour: Int): String {
    val heavenlyStems = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    val earthlyBranches = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    val branchIndex = ((hour + 1) % 24) / 2
    // 按“甲己起甲子，乙庚起丙子……”确定各日干在子时的起始天干
    val startStemIndex = when (dayGan) {
        "甲", "己" -> 0
        "乙", "庚" -> 2
        "丙", "辛" -> 4
        "丁", "壬" -> 6
        "戊", "癸" -> 8
        else -> 0
    }
    val stemIndex = (startStemIndex + branchIndex) % heavenlyStems.size
    return heavenlyStems[stemIndex] + earthlyBranches[branchIndex]
}
