历法计算仓库 data/calendar

文件 data/calendar/CnCalendarRepository.kt，唯一直接调用 lunar-java 的文件，页面不直接调第三方库。

支持范围
公历 1901..2100，农历 1900..2100，农历以同一公历边界过滤实际可选月日。年份列表在初始化时预生成。

错误建模 CalendarQueryResult 密封接口
Success 携带 value，NotFound 业务未命中，CalculationError 携带 throwable 技术异常。仓库必须区分三者，禁止用 runCatching 吞成一种。

数据类 CalendarRecord
solarYear solarMonth solarDay 公历，lunarYear lunarMonth lunarDay 农历，isLeapMonth 是否闰月，term 节气名称可空。
扩展 toCalendarSummary 转历法摘要，toLunarDate 转农历日期。

仓库公共方法（全部 suspend，内部 calculate 在 Dispatchers.Default 执行并统一异常处理）
queryCalendarBySolar 公历转农历加节气。
queryCalendarByLunar 农历转公历加节气。
querySolarYears、querySolarMonths(year)、querySolarDays(year, month) 公历选项范围。
queryLunarYears、queryLunarMonths(year) 返回 LunarMonthOption 含闰月、queryLunarDays(year, month, isLeap) 农历选项范围。

干支摘要 buildGanzhiSummary
输入 CalendarRecord 与 hour minute，输出年月日时四柱文本。
经 Solar.fromYmdHms 转 Lunar，取 yearInGanZhiExact 立春切换的年干支、monthInGanZhiExact 节气切换的月干支、dayInGanZhi 日干支，时柱调 buildHourGanzhi(lunar.dayGan, hour)。
关键：六爻干支年按立春交接更替，用 Exact 系列，不用农历正月初一，二者不得混用。

lunar-java 交互 API
Solar：fromYmd、fromYmdHms、fromJulianDay、lunar、year month day、next(days)。
Lunar：fromYmd 负月份为闰月、solar、year month day、jieQi、yearInGanZhiExact、monthInGanZhiExact、dayInGanZhi、dayGan。
LunarYear：fromYear、getMonthsInYear。
LunarMonth：fromYm、month 负为闰、dayCount、firstJulianDay。

选项过滤机制
公历年月日与农历年月日均级联依赖，修改上级重查下级列表，原选值不在新范围则回退到列表首项。农历月用 LunarMonthOption 正确处理闰月，日按该月 dayCount 与公历边界双重过滤。

内置常量
公历年范围、农历年范围、中文数字映射、农历月份名正月到腊月、农历日名初一到三十。
