时间领域模型 domain/time

文件 domain/time/TimeModels.kt，纯 Kotlin 无 Android 依赖。

时间类型枚举 DivinationTimeType
GREGORIAN 公历，LUNAR 农历，GANZHI 干支。三种起卦时间模式的统一标识。

数据类
SolarDateTime：year month day hour minute 五个 Int。公历日期时间。
LunarDate：year month day 三个 Int 加 isLeapMonth 布尔。农历日期。
LunarMonthOption：month Int 加 isLeapMonth 布尔，计算属性 label 显示文本（闰月带闰字），id 唯一标识格式为月加冒号加 0 或 1。供农历月份下拉使用。
DivinationTime：type 时间类型，solarDateTime 公历时间，value 格式化后的时间文本。首页时间行与结果页展示用。
CalendarSummary：lunarYearText lunarMonthText lunarDayText termText ganzhiSummaryText。历法摘要，首页农历卡片用。

顶层函数
currentGregorianDivinationTime：取系统当前公历时间构建 DivinationTime。
buildDivinationTime：按指定类型构建 DivinationTime。
formatSolarDivinationTime、formatLunarDivinationTime、formatGanzhiDivinationTime：三种模式的时间文本格式化。
formatDisplayLunarMonth、formatSelectableLunarMonth：农历月份格式化，含闰月处理。
buildHourGanzhi：核心，按日干与小时推时辰干支。

时辰干支推算 buildHourGanzhi
输入日干与 24 小时制时数，输出天干加地支。
地支索引为 (hour 加 1) 除以 2 再对 12 取模，加 1 是把 23 点到 0 点归入子时。
子时起始天干按五鼠遁：甲己起甲子 startStemIndex 0，乙庚起丙子 2，丙辛起戊子 4，丁壬起庚子 6，戊癸起壬子 8。
天干索引为 (startStemIndex 加 branchIndex) 对 10 取模。
本函数供 data/calendar 的 buildGanzhiSummary 调用，公历农历起卦时时柱现算不手写推导。
