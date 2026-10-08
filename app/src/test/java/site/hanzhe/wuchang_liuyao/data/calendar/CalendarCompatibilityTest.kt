package site.hanzhe.wuchang_liuyao.data.calendar

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationDateInfo
import site.hanzhe.wuchang_liuyao.domain.divination.GanzhiPillar
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.domain.time.LunarDate
import site.hanzhe.wuchang_liuyao.domain.time.LunarMonthOption
import site.hanzhe.wuchang_liuyao.domain.time.SolarDateTime
import site.hanzhe.wuchang_liuyao.domain.time.formatLunarDivinationTime
import site.hanzhe.wuchang_liuyao.domain.time.formatSolarDivinationTime
import site.hanzhe.wuchang_liuyao.feature.home.HomeUiState
import site.hanzhe.wuchang_liuyao.feature.time.GanzhiSelectionState
import site.hanzhe.wuchang_liuyao.feature.time.TimeSelectionHostUiState
import site.hanzhe.wuchang_liuyao.feature.time.TimeSelectionUiState
import site.hanzhe.wuchang_liuyao.navigation.DivinationRequestBuildResult
import site.hanzhe.wuchang_liuyao.navigation.buildDivinationRequest
import java.time.LocalDateTime
import kotlin.math.abs

/** 用固定的旧版历法快照验证日期转换和排盘口径。 */
class CalendarCompatibilityTest {
    private val repository = CnCalendarRepository()
    private val fixtures = readFixtures()

    /** 核对春节、闰月和节气边界的双向转换及首页摘要。 */
    @Test
    fun calendarConversionsAndSummariesMatchSnapshots() = runBlocking {
        fixtures.forEach { fixture ->
            val solar = fixture.solarDateTime
            val record = repository.queryCalendarBySolar(solar.year, solar.month, solar.day).valueOrFail()
            assertEquals("农历日期：$solar", fixture.lunarDate, record.toLunarDate())
            assertEquals("当天节气：$solar", fixture.termText, record.term.orEmpty())
            assertEquals(
                "干支摘要：$solar",
                fixture.summary,
                buildGanzhiSummary(record, solar.hour, solar.minute)
            )
            val lunar = fixture.lunarDate
            assertEquals(
                "农历反查：$solar",
                record,
                repository.queryCalendarByLunar(lunar.year, lunar.month, lunar.day, lunar.isLeapMonth).valueOrFail()
            )
        }
    }

    /** 核对公历和农历起卦在两种日界设置下的四柱及日期展示。 */
    @Test
    fun divinationPillarsAndDateTextsMatchSnapshots() {
        fixtures.forEach { fixture ->
            listOf(DivinationTimeType.GREGORIAN, DivinationTimeType.LUNAR).forEach { type ->
                listOf(false, true).forEach { changeDay ->
                    val dateInfo = buildDateInfo(fixture.toSelectionState(type), changeDay)
                    val expected = if (changeDay) fixture.nextDayPillars else fixture.civilDayPillars
                    assertEquals("四柱：${fixture.solarDateTime}，$type，换日=$changeDay", expected, dateInfo.pillars())
                    assertEquals("节气不随换日偏移", fixture.termText, dateInfo.termText)
                    val solar = fixture.solarDateTime
                    val solarText = formatSolarDivinationTime(solar)
                    val lunarText = formatLunarDivinationTime(fixture.lunarDate, solar.hour, solar.minute)
                    assertEquals("公历文本保留输入日期", solarText, dateInfo.solarText)
                    assertEquals("农历文本保留输入日期", lunarText, dateInfo.lunarText)
                    assertEquals(
                        "展示文本使用选定的时间类型",
                        if (type == DivinationTimeType.GREGORIAN) solarText else lunarText,
                        dateInfo.displayText
                    )
                }
            }
        }
    }

    /** 可选年月日必须保留闰月身份并裁剪到公历支持范围。 */
    @Test
    fun selectableDatesKeepLeapMonthsAndSupportedBoundaries() = runBlocking {
        assertEquals((1901..2100).toList(), repository.querySolarYears().valueOrFail())
        assertEquals((1900..2100).toList(), repository.queryLunarYears().valueOrFail())
        assertEquals((1..12).toList(), repository.querySolarMonths(2024).valueOrFail())
        assertEquals((1..29).toList(), repository.querySolarDays(2024, 2).valueOrFail())
        assertEquals((1..28).toList(), repository.querySolarDays(2023, 2).valueOrFail())
        val leapMonths = listOf(LunarMonthOption(1, false), LunarMonthOption(2, false), LunarMonthOption(2, true)) +
            (3..12).map { LunarMonthOption(it, false) }
        assertEquals(leapMonths, repository.queryLunarMonths(2023).valueOrFail())
        assertEquals((1..29).toList(), repository.queryLunarDays(2023, 2, true).valueOrFail())
        assertEquals(
            listOf(LunarMonthOption(11, false), LunarMonthOption(12, false)),
            repository.queryLunarMonths(1900).valueOrFail()
        )
        assertEquals(11, repository.queryLunarDays(1900, 11, false).valueOrFail().first())
        assertEquals(listOf(1), repository.queryLunarDays(2100, 12, false).valueOrFail())
        assertTrue(repository.queryLunarMonths(2033).valueOrFail().contains(LunarMonthOption(11, true)))
    }

    /** 无效日期和超出范围的日期仍作为业务未命中返回。 */
    @Test
    fun invalidDatesRemainNotFound() = runBlocking {
        assertEquals(CalendarQueryResult.NotFound, repository.queryCalendarBySolar(1900, 12, 31))
        assertEquals(CalendarQueryResult.NotFound, repository.queryCalendarBySolar(2101, 1, 1))
        assertEquals(CalendarQueryResult.NotFound, repository.queryCalendarBySolar(2023, 2, 29))
        assertEquals(CalendarQueryResult.NotFound, repository.queryCalendarBySolar(2024, 13, 1))
        assertEquals(CalendarQueryResult.NotFound, repository.queryCalendarByLunar(2024, 2, 1, true))
        assertEquals(CalendarQueryResult.NotFound, repository.queryCalendarByLunar(2023, 2, 30, true))
        assertEquals(CalendarQueryResult.NotFound, repository.queryCalendarByLunar(2024, 0, 1, false))
        assertEquals(CalendarQueryResult.NotFound, repository.queryLunarDays(2024, 2, true))
        assertEquals(CalendarQueryResult.NotFound, repository.queryLunarMonths(1899))
        assertEquals(CalendarQueryResult.NotFound, repository.querySolarMonths(2101))
    }

    /** 干支手选必须保留空缺字段，不补算公农历和时柱。 */
    @Test
    fun manualGanzhiKeepsMissingDateFields() {
        val selection = fixtures.first().toSelectionState(DivinationTimeType.GANZHI).copy(
            ganzhiSelectionState = GanzhiSelectionState(monthBranch = "寅", dayStem = "甲", dayBranch = "子")
        )
        val dateInfo = buildDateInfo(selection, false)
        assertEquals(dateInfo, buildDateInfo(selection, true))
        assertEquals(GanzhiPillar(), dateInfo.year)
        assertNull(dateInfo.month.heavenlyStem)
        assertEquals("寅", dateInfo.month.earthlyBranch)
        assertEquals("甲子", dateInfo.day.text)
        assertEquals(GanzhiPillar(), dateInfo.hour)
        assertEquals("", dateInfo.termText)
        assertEquals("用户使用干支时间起卦", dateInfo.solarText)
        assertEquals("用户使用干支时间起卦", dateInfo.lunarText)
    }

    /** 读取与第三方库运行时无关的固定样例。 */
    private fun readFixtures(): List<CalendarFixture> {
        val stream = requireNotNull(javaClass.getResourceAsStream("/calendar/legacy-calendar-fixtures.tsv")) {
            "未找到历法回归样例"
        }
        return stream.bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter { it.isNotBlank() && !it.startsWith("#") }.map { line ->
                val columns = line.split('\t')
                val time = LocalDateTime.parse(columns[0])
                val signedMonth = columns[2].toInt()
                CalendarFixture(
                    solarDateTime = SolarDateTime(time.year, time.monthValue, time.dayOfMonth, time.hour, time.minute),
                    lunarDate = LunarDate(columns[1].toInt(), abs(signedMonth), columns[3].toInt(), signedMonth < 0),
                    termText = columns[4].takeUnless { it == "-" }.orEmpty(),
                    summary = columns[5],
                    civilDayPillars = columns[6].split(','),
                    nextDayPillars = columns[7].split(',')
                )
            }.toList()
        }
    }

    /** 从固定样例建立与页面确认状态一致的输入。 */
    private fun CalendarFixture.toSelectionState(type: DivinationTimeType): TimeSelectionUiState {
        return TimeSelectionUiState(
            selectedType = type,
            solarDateTime = solarDateTime,
            lunarDate = lunarDate,
            ganzhiText = summary,
            solarYearOptions = emptyList(),
            solarMonthOptions = emptyList(),
            solarDayOptions = emptyList(),
            lunarYearOptions = emptyList(),
            lunarMonthOptions = emptyList(),
            lunarDayOptions = emptyList()
        )
    }

    /** 通过正式请求组装入口获取排盘时间信息。 */
    private fun buildDateInfo(selection: TimeSelectionUiState, changeDay: Boolean): DivinationDateInfo {
        val result = buildDivinationRequest(
            homeUiState = HomeUiState(selectedYaoValues = List(6) { "少阳" }),
            timeUiState = TimeSelectionHostUiState(confirmedTimeSelectionUiState = selection),
            changeDayPillarAt23 = changeDay
        )
        assertTrue("请求组装失败：$result", result is DivinationRequestBuildResult.Success)
        return (result as DivinationRequestBuildResult.Success).request.dateInfo
    }

    /** 将领域四柱转换成可与固定样例比较的顺序。 */
    private fun DivinationDateInfo.pillars(): List<String> {
        return listOf(year.text, month.text, day.text, hour.text)
    }

    /** 保留查询错误的类型信息以便定位回归。 */
    private fun <T> CalendarQueryResult<T>.valueOrFail(): T {
        assertTrue("历法查询失败：$this", this is CalendarQueryResult.Success)
        return (this as CalendarQueryResult.Success).value
    }

    private data class CalendarFixture(
        val solarDateTime: SolarDateTime,
        val lunarDate: LunarDate,
        val termText: String,
        val summary: String,
        val civilDayPillars: List<String>,
        val nextDayPillars: List<String>
    )
}
