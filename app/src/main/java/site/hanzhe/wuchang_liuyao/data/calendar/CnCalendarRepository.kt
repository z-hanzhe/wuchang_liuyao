package site.hanzhe.wuchang_liuyao.data.calendar

import com.nlf.calendar.Lunar
import com.nlf.calendar.LunarMonth
import com.nlf.calendar.LunarYear
import com.nlf.calendar.Solar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import site.hanzhe.wuchang_liuyao.domain.time.CalendarSummary
import site.hanzhe.wuchang_liuyao.domain.time.LunarDate
import site.hanzhe.wuchang_liuyao.domain.time.LunarMonthOption
import site.hanzhe.wuchang_liuyao.domain.time.buildHourGanzhi
import kotlin.math.abs

private const val MinSolarYear = 1901
private const val MaxSolarYear = 2100
// 公历边界两端可能落在相邻农历年，农历选项按公历支持范围继续过滤
private const val MinLunarYear = 1900
private const val MaxLunarYear = 2100

private val MinSupportedSolar = Solar.fromYmd(MinSolarYear, 1, 1)
private val MaxSupportedSolar = Solar.fromYmd(MaxSolarYear, 12, 31)
private val SolarYearOptions = (MinSolarYear..MaxSolarYear).toList()
private val LunarYearOptions = (MinLunarYear..MaxLunarYear).toList()

internal sealed interface CalendarQueryResult<out T> {
    data class Success<T>(val value: T) : CalendarQueryResult<T>
    data object NotFound : CalendarQueryResult<Nothing>
    data class CalculationError(val throwable: Throwable) : CalendarQueryResult<Nothing>
}

internal data class CalendarRecord(
    val solarYear: Int,
    val solarMonth: Int,
    val solarDay: Int,
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val isLeapMonth: Boolean,
    val term: String?
)

internal class CnCalendarRepository {
    suspend fun queryCalendarBySolar(
        solarYear: Int,
        solarMonth: Int,
        solarDay: Int
    ): CalendarQueryResult<CalendarRecord> {
        return calculate {
            val solar = Solar.fromYmd(solarYear, solarMonth, solarDay)
            if (!solar.isSupportedSolarDate()) {
                null
            } else {
                solar.lunar.toCalendarRecord()
            }
        }
    }

    suspend fun queryCalendarByLunar(
        lunarYear: Int,
        lunarMonth: Int,
        lunarDay: Int,
        isLeapMonth: Boolean
    ): CalendarQueryResult<CalendarRecord> {
        return calculate {
            if (lunarYear !in MinLunarYear..MaxLunarYear || lunarMonth !in 1..12) {
                return@calculate null
            }
            val lunar = Lunar.fromYmd(lunarYear, lunarMonth.toSignedLunarMonth(isLeapMonth), lunarDay)
            val solar = lunar.solar
            if (!solar.isSupportedSolarDate()) {
                null
            } else {
                lunar.toCalendarRecord()
            }
        }
    }

    suspend fun querySolarYears(): CalendarQueryResult<List<Int>> {
        return CalendarQueryResult.Success(SolarYearOptions)
    }

    suspend fun querySolarMonths(year: Int): CalendarQueryResult<List<Int>> {
        return calculate {
            if (year !in MinSolarYear..MaxSolarYear) {
                null
            } else {
                (1..12)
                    .filter { month -> hasSupportedSolarDay(year, month) }
                    .ifEmpty { null }
            }
        }
    }

    suspend fun querySolarDays(year: Int, month: Int): CalendarQueryResult<List<Int>> {
        return calculate {
            if (year !in MinSolarYear..MaxSolarYear || month !in 1..12) {
                null
            } else {
                buildList {
                    (1..31).forEach { day ->
                        val solar = createSolarOrNull(year, month, day)
                        if (solar?.isSupportedSolarDate() == true) {
                            add(day)
                        }
                    }
                }.ifEmpty { null }
            }
        }
    }

    suspend fun queryLunarYears(): CalendarQueryResult<List<Int>> {
        return CalendarQueryResult.Success(LunarYearOptions)
    }

    suspend fun queryLunarMonths(year: Int): CalendarQueryResult<List<LunarMonthOption>> {
        return calculate {
            if (year !in MinLunarYear..MaxLunarYear) {
                null
            } else {
                LunarYear.fromYear(year)
                    .getMonthsInYear()
                    .filter { it.hasSupportedSolarDay() }
                    .map {
                        LunarMonthOption(
                            month = abs(it.month),
                            isLeapMonth = it.month < 0
                        )
                    }
                    .ifEmpty { null }
            }
        }
    }

    suspend fun queryLunarDays(
        year: Int,
        month: Int,
        isLeapMonth: Boolean
    ): CalendarQueryResult<List<Int>> {
        return calculate {
            val lunarMonth = LunarMonth.fromYm(year, month.toSignedLunarMonth(isLeapMonth)) ?: return@calculate null
            (1..lunarMonth.dayCount)
                .filter { day ->
                    Lunar.fromYmd(year, lunarMonth.month, day).solar.isSupportedSolarDate()
                }
                .ifEmpty { null }
        }
    }

    private suspend fun <T> calculate(block: () -> T?): CalendarQueryResult<T> {
        return withContext(Dispatchers.Default) {
            try {
                val value = block()
                if (value == null) {
                    CalendarQueryResult.NotFound
                } else {
                    CalendarQueryResult.Success(value)
                }
            } catch (throwable: IllegalArgumentException) {
                CalendarQueryResult.NotFound
            } catch (throwable: Throwable) {
                CalendarQueryResult.CalculationError(throwable)
            }
        }
    }
}

internal fun CalendarRecord.toCalendarSummary(
    hour: Int,
    minute: Int
): CalendarSummary {
    return CalendarSummary(
        lunarYearText = formatLunarYear(lunarYear),
        lunarMonthText = formatLunarMonth(lunarMonth, isLeapMonth),
        lunarDayText = formatLunarDay(lunarDay),
        termText = term.orEmpty(),
        ganzhiSummaryText = buildGanzhiSummary(this, hour, minute)
    )
}

internal fun CalendarRecord.toLunarDate(): LunarDate {
    return LunarDate(
        year = lunarYear,
        month = lunarMonth,
        day = lunarDay,
        isLeapMonth = isLeapMonth
    )
}

internal fun buildGanzhiSummary(
    calendarRecord: CalendarRecord,
    hour: Int,
    minute: Int
): String {
    val lunar = calendarRecord.toLunarAt(hour, minute)
    val yearGanZhi = lunar.yearInGanZhiExact
    val monthGanZhi = lunar.monthInGanZhiExact
    val dayGanZhi = lunar.dayInGanZhi
    val hourGanZhi = buildHourGanzhi(lunar.dayGan, hour)
    // 六爻干支年按立春节气交接时刻更替，和农历年正月初一更替分开处理
    return "${yearGanZhi}年    ${monthGanZhi}月    ${dayGanZhi}日    ${hourGanZhi}时"
}

private fun Lunar.toCalendarRecord(): CalendarRecord {
    val solar = solar
    val lunarMonth = month
    val jieQi = jieQi
    return CalendarRecord(
        solarYear = solar.year,
        solarMonth = solar.month,
        solarDay = solar.day,
        lunarYear = year,
        lunarMonth = abs(lunarMonth),
        lunarDay = day,
        isLeapMonth = lunarMonth < 0,
        term = jieQi.ifBlank { null }
    )
}

private fun CalendarRecord.toLunarAt(hour: Int, minute: Int): Lunar {
    return Solar.fromYmdHms(solarYear, solarMonth, solarDay, hour, minute, 0).lunar
}

private fun hasSupportedSolarDay(year: Int, month: Int): Boolean {
    return (1..31).any { day ->
        createSolarOrNull(year, month, day)?.isSupportedSolarDate() == true
    }
}

private fun createSolarOrNull(year: Int, month: Int, day: Int): Solar? {
    return try {
        Solar.fromYmd(year, month, day)
    } catch (throwable: IllegalArgumentException) {
        null
    }
}

private fun LunarMonth.hasSupportedSolarDay(): Boolean {
    val firstSolar = Solar.fromJulianDay(firstJulianDay)
    val lastSolar = firstSolar.next(dayCount - 1)
    return firstSolar.isOnOrBefore(MaxSupportedSolar) && lastSolar.isOnOrAfter(MinSupportedSolar)
}

private fun Solar.isSupportedSolarDate(): Boolean {
    return isOnOrAfter(MinSupportedSolar) && isOnOrBefore(MaxSupportedSolar)
}

private fun Solar.isOnOrBefore(other: Solar): Boolean {
    return compareDateTo(other) <= 0
}

private fun Solar.isOnOrAfter(other: Solar): Boolean {
    return compareDateTo(other) >= 0
}

private fun Solar.compareDateTo(other: Solar): Int {
    return when {
        year != other.year -> year.compareTo(other.year)
        month != other.month -> month.compareTo(other.month)
        else -> day.compareTo(other.day)
    }
}

private fun Int.toSignedLunarMonth(isLeapMonth: Boolean): Int {
    return if (isLeapMonth) -this else this
}

private fun formatLunarYear(year: Int): String {
    val chineseDigits = mapOf(
        '0' to '〇',
        '1' to '一',
        '2' to '二',
        '3' to '三',
        '4' to '四',
        '5' to '五',
        '6' to '六',
        '7' to '七',
        '8' to '八',
        '9' to '九'
    )
    return buildString {
        year.toString().forEach { digit ->
            append(chineseDigits.getValue(digit))
        }
        append('年')
    }
}

private fun formatLunarMonth(month: Int, isLeapMonth: Boolean): String {
    val monthText = when (month) {
        1 -> "正月"
        2 -> "二月"
        3 -> "三月"
        4 -> "四月"
        5 -> "五月"
        6 -> "六月"
        7 -> "七月"
        8 -> "八月"
        9 -> "九月"
        10 -> "十月"
        11 -> "冬月"
        12 -> "腊月"
        else -> "${month}月"
    }
    return if (isLeapMonth) "闰$monthText" else monthText
}

private fun formatLunarDay(day: Int): String {
    return when (day) {
        1 -> "初一"
        2 -> "初二"
        3 -> "初三"
        4 -> "初四"
        5 -> "初五"
        6 -> "初六"
        7 -> "初七"
        8 -> "初八"
        9 -> "初九"
        10 -> "初十"
        11 -> "十一"
        12 -> "十二"
        13 -> "十三"
        14 -> "十四"
        15 -> "十五"
        16 -> "十六"
        17 -> "十七"
        18 -> "十八"
        19 -> "十九"
        20 -> "二十"
        21 -> "廿一"
        22 -> "廿二"
        23 -> "廿三"
        24 -> "廿四"
        25 -> "廿五"
        26 -> "廿六"
        27 -> "廿七"
        28 -> "廿八"
        29 -> "廿九"
        30 -> "三十"
        else -> day.toString()
    }
}
