package site.hanzhe.wuchang_liuyao.domain.divination

private val SixGodSequence = listOf("青龙", "朱雀", "勾陈", "螣蛇", "白虎", "玄武")
private val SixRelativeSequence = listOf("兄弟", "父母", "官鬼", "妻财", "子孙")
private val FiveElements = listOf("木", "火", "土", "金", "水")
private val PalaceNames = listOf("乾", "兑", "离", "震", "巽", "坎", "艮", "坤")
private val PalaceElements = listOf("金", "金", "火", "木", "木", "水", "土", "土")
private val TrigramMarks = listOf("111", "110", "101", "100", "011", "010", "001", "000")
private val HeavenlyStems = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
private val EarthlyBranches = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
private val BranchElements = listOf("水", "土", "木", "木", "土", "火", "火", "土", "金", "金", "土", "水")
private val XunKongBranches = listOf("子丑", "寅卯", "辰巳", "午未", "申酉", "戌亥")
private val NoblemanByStem = mapOf(
    "甲" to "丑未",
    "戊" to "丑未",
    "乙" to "子申",
    "己" to "子申",
    "丙" to "亥酉",
    "丁" to "亥酉",
    "庚" to "午寅",
    "辛" to "午寅",
    "壬" to "卯巳",
    "癸" to "卯巳"
)
private val LuByStem = mapOf(
    "甲" to "寅",
    "乙" to "卯",
    "丙" to "巳",
    "戊" to "巳",
    "丁" to "午",
    "己" to "午",
    "庚" to "申",
    "辛" to "酉",
    "壬" to "亥",
    "癸" to "子"
)
private val YangBladeByStem = mapOf(
    "甲" to "卯",
    "乙" to "寅",
    "丙" to "午",
    "戊" to "午",
    "丁" to "巳",
    "己" to "巳",
    "庚" to "酉",
    "辛" to "申",
    "壬" to "子",
    "癸" to "亥"
)
private val WenChangByStem = mapOf(
    "甲" to "巳",
    "乙" to "午",
    "丙" to "申",
    "戊" to "申",
    "丁" to "酉",
    "己" to "酉",
    "庚" to "亥",
    "辛" to "子",
    "壬" to "寅",
    "癸" to "卯"
)
private val PalaceNaJia = listOf(
    Pair("甲", "子寅辰") to Pair("壬", "午申戌"),
    Pair("丁", "巳卯丑") to Pair("丁", "亥酉未"),
    Pair("己", "卯丑亥") to Pair("己", "酉未巳"),
    Pair("庚", "子寅辰") to Pair("庚", "午申戌"),
    Pair("辛", "丑亥酉") to Pair("辛", "未巳卯"),
    Pair("戊", "寅辰午") to Pair("戊", "申戌子"),
    Pair("丙", "辰午申") to Pair("丙", "戌子寅"),
    Pair("乙", "未巳卯") to Pair("癸", "丑亥酉")
)
private val SixCombinationNames = listOf("否", "困", "旅", "豫", "节", "贲", "复", "泰")
private val YangLineSymbol = "━━━━"
private val YinLineSymbol = "━　━"

private val HexagramNames = mapOf(
    "111111" to "乾为天",
    "011111" to "天风姤",
    "001111" to "天山遁",
    "000111" to "天地否",
    "000011" to "风地观",
    "000001" to "山地剥",
    "000101" to "火地晋",
    "111101" to "火天大有",
    "110110" to "兑为泽",
    "010110" to "泽水困",
    "000110" to "泽地萃",
    "001110" to "泽山咸",
    "001010" to "水山蹇",
    "001000" to "地山谦",
    "001100" to "雷山小过",
    "110100" to "雷泽归妹",
    "101101" to "离为火",
    "001101" to "火山旅",
    "011101" to "火风鼎",
    "010101" to "火水未济",
    "010001" to "山水蒙",
    "010011" to "风水涣",
    "010111" to "天水讼",
    "101111" to "天火同人",
    "100100" to "震为雷",
    "000100" to "雷地豫",
    "010100" to "雷水解",
    "011100" to "雷风恒",
    "011000" to "地风升",
    "011010" to "水风井",
    "011110" to "泽风大过",
    "100110" to "泽雷随",
    "011011" to "巽为风",
    "111011" to "风天小畜",
    "101011" to "风火家人",
    "100011" to "风雷益",
    "100111" to "天雷无妄",
    "100101" to "火雷噬嗑",
    "100001" to "山雷颐",
    "011001" to "山风蛊",
    "010010" to "坎为水",
    "110010" to "水泽节",
    "100010" to "水雷屯",
    "101010" to "水火既济",
    "101110" to "泽火革",
    "101100" to "雷火丰",
    "101000" to "地火明夷",
    "010000" to "地水师",
    "001001" to "艮为山",
    "101001" to "山火贲",
    "111001" to "山天大畜",
    "110001" to "山泽损",
    "110101" to "火泽睽",
    "110111" to "天泽履",
    "110011" to "风泽中孚",
    "001011" to "风山渐",
    "000000" to "坤为地",
    "100000" to "地雷复",
    "110000" to "地泽临",
    "111000" to "地天泰",
    "111100" to "雷天大壮",
    "111110" to "泽天夬",
    "111010" to "水天需",
    "000010" to "水地比"
)

private data class GanZhiValue(
    val stem: String,
    val branch: String
)

private data class HiddenLineValue(
    val relative: String,
    val stem: String,
    val branch: String,
    val element: String
)

private data class HexagramValue(
    val mark: String,
    val name: String,
    val naJia: List<GanZhiValue>,
    val relatives: List<String>,
    val elements: List<String>
)

internal class HexagramCalculator {

    fun calculate(request: DivinationRequest): DivinationResult {
        require(request.linesTopDown.size == 6) { "六爻数量不正确" }
        val linesBottomUp = request.linesTopDown.reversed()
        val baseMark = linesBottomUp.joinToString(separator = "") { it.baseMark.toString() }
        val shiYing = resolveShiYing(baseMark)
        val palaceIndex = resolvePalaceIndex(baseMark, shiYing.first)
        val baseHexagram = buildHexagram(baseMark, palaceIndex)
        val sixGods = resolveSixGods(request.dateInfo.day.heavenlyStem)
        val spirits = resolveCommonSpirits(request.dateInfo)
        val dayXunKong = resolveDayXunKong(request.dateInfo.day)
        val hiddenLines = resolveHiddenLines(
            palaceIndex = palaceIndex
        )
        val changedMark = if (linesBottomUp.any { it.isChanging }) {
            linesBottomUp.joinToString(separator = "") { it.changedMark.toString() }
        } else {
            null
        }
        val changedHexagram = changedMark?.let { mark ->
            buildHexagram(mark, palaceIndex)
        }
        val changedDisplayPalaceIndex = changedMark?.let { mark ->
            val changedShiYing = resolveShiYing(mark)
            resolvePalaceIndex(mark, changedShiYing.first)
        }
        val changedDisplayHexagramType = changedMark?.let { mark ->
            resolveHexagramType(mark)
        }

        val rowsTopDown = buildList {
            repeat(6) { topIndex ->
                val bottomIndex = 5 - topIndex
                val line = linesBottomUp[bottomIndex]
                val hiddenLine = hiddenLines[bottomIndex]
                val baseNaJia = baseHexagram.naJia[bottomIndex]
                val changedNaJia = changedHexagram?.naJia?.get(bottomIndex)
                add(
                    DivinationTableRow(
                        sixGod = sixGods[bottomIndex],
                        hiddenRelative = hiddenLine?.relative.orEmpty(),
                        hiddenStem = hiddenLine?.stem.orEmpty(),
                        hiddenBranch = hiddenLine?.branch.orEmpty(),
                        hiddenElement = hiddenLine?.element.orEmpty(),
                        baseRelative = baseHexagram.relatives[bottomIndex],
                        baseStem = baseNaJia.stem,
                        baseBranch = baseNaJia.branch,
                        baseElement = baseHexagram.elements[bottomIndex],
                        baseLineSymbol = line.toBaseLineSymbol(),
                        shiYingMark = when (bottomIndex + 1) {
                            shiYing.first -> "世"
                            shiYing.second -> "应"
                            else -> ""
                        },
                        changingSymbol = line.changeSymbol,
                        changedLineSymbol = changedHexagram?.mark?.get(bottomIndex)?.toLineSymbol().orEmpty(),
                        changedRelative = changedHexagram?.relatives?.get(bottomIndex).orEmpty(),
                        changedStem = changedNaJia?.stem.orEmpty(),
                        changedBranch = changedNaJia?.branch.orEmpty(),
                        changedElement = changedHexagram?.elements?.get(bottomIndex).orEmpty()
                    )
                )
            }
        }

        return DivinationResult(
            question = request.question,
            methodLabel = request.methodLabel,
            dateInfo = request.dateInfo,
            spirits = spirits,
            dayXunKong = dayXunKong,
            baseHexagramName = baseHexagram.name,
            palaceName = PalaceNames[palaceIndex],
            hexagramType = resolveHexagramType(baseMark),
            changedHexagramName = changedHexagram?.name,
            changedPalaceName = changedDisplayPalaceIndex?.let { PalaceNames[it] },
            changedHexagramType = changedDisplayHexagramType,
            rowsTopDown = rowsTopDown
        )
    }

    private fun buildHexagram(mark: String, palaceIndex: Int): HexagramValue {
        val naJia = resolveNaJia(mark)
        val relatives = naJia.map { naJiaValue ->
            resolveSixRelative(
                palaceElement = PalaceElements[palaceIndex],
                branchElement = branchToElement(naJiaValue.branch)
            )
        }
        val elements = naJia.map { branchToElement(it.branch) }
        return HexagramValue(
            mark = mark,
            name = HexagramNames.getValue(mark),
            naJia = naJia,
            relatives = relatives,
            elements = elements
        )
    }

    private fun resolveSixGods(dayStem: String?): List<String> {
        val requiredDayStem = requireNotNull(dayStem) { "日干不能为空" }
        val stemIndex = HeavenlyStems.indexOf(requiredDayStem)
        require(stemIndex >= 0) { "不支持的日干：$requiredDayStem" }
        val startIndex = when (requiredDayStem) {
            "甲", "乙" -> 0
            "丙", "丁" -> 1
            "戊" -> 2
            "己" -> 3
            "庚", "辛" -> 4
            "壬", "癸" -> 5
            else -> 0
        }
        return List(6) { index ->
            SixGodSequence[(startIndex + index) % SixGodSequence.size]
        }
    }

    private fun resolveCommonSpirits(dateInfo: DivinationDateInfo): List<DivinationSpirit> {
        val dayStem = requireNotNull(dateInfo.day.heavenlyStem) { "日干不能为空" }
        val dayBranch = requireNotNull(dateInfo.day.earthlyBranch) { "日支不能为空" }
        val monthBranch = requireNotNull(dateInfo.month.earthlyBranch) { "月支不能为空" }
        return listOf(
            DivinationSpirit("禄神", resolveByStem(dayStem, LuByStem)),
            DivinationSpirit("羊刃", resolveByStem(dayStem, YangBladeByStem)),
            DivinationSpirit("文昌", resolveByStem(dayStem, WenChangByStem)),
            DivinationSpirit("驿马", resolveByDayBranchGroup(dayBranch, "寅", "亥", "申", "巳")),
            DivinationSpirit("桃花", resolveByDayBranchGroup(dayBranch, "酉", "午", "卯", "子")),
            DivinationSpirit("将星", resolveByDayBranchGroup(dayBranch, "子", "酉", "午", "卯")),
            DivinationSpirit("劫煞", resolveByDayBranchGroup(dayBranch, "巳", "寅", "亥", "申")),
            DivinationSpirit("灾煞", resolveByDayBranchGroup(dayBranch, "午", "卯", "子", "酉")),
            DivinationSpirit("华盖", resolveByDayBranchGroup(dayBranch, "辰", "丑", "戌", "未")),
            DivinationSpirit("谋星", resolveByDayBranchGroup(dayBranch, "戌", "未", "辰", "丑")),
            DivinationSpirit("天医", resolveTianYi(monthBranch)),
            DivinationSpirit("天喜", resolveTianXi(monthBranch)),
            DivinationSpirit("贵人", resolveByStem(dayStem, NoblemanByStem))
        )
    }

    private fun resolveByStem(
        stem: String,
        values: Map<String, String>
    ): String {
        return requireNotNull(values[stem]) { "不支持的日干：$stem" }
    }

    private fun resolveByDayBranchGroup(
        dayBranch: String,
        shenZiChen: String,
        siYouChou: String,
        yinWuXu: String,
        haiMaoWei: String
    ): String {
        return when (dayBranch) {
            "申", "子", "辰" -> shenZiChen
            "巳", "酉", "丑" -> siYouChou
            "寅", "午", "戌" -> yinWuXu
            "亥", "卯", "未" -> haiMaoWei
            else -> error("不支持的日支：$dayBranch")
        }
    }

    private fun resolveTianYi(monthBranch: String): String {
        val monthIndex = EarthlyBranches.indexOf(monthBranch)
        require(monthIndex >= 0) { "不支持的月支：$monthBranch" }
        return EarthlyBranches[(monthIndex + EarthlyBranches.lastIndex) % EarthlyBranches.size]
    }

    private fun resolveTianXi(monthBranch: String): String {
        return when (monthBranch) {
            "寅", "卯", "辰" -> "戌"
            "巳", "午", "未" -> "丑"
            "申", "酉", "戌" -> "辰"
            "亥", "子", "丑" -> "未"
            else -> error("不支持的月支：$monthBranch")
        }
    }

    private fun resolveDayXunKong(day: GanzhiPillar): String {
        val stem = requireNotNull(day.heavenlyStem) { "日干不能为空" }
        val branch = requireNotNull(day.earthlyBranch) { "日支不能为空" }
        val stemIndex = HeavenlyStems.indexOf(stem)
        val branchIndex = EarthlyBranches.indexOf(branch)
        require(stemIndex >= 0) { "不支持的日干：$stem" }
        require(branchIndex >= 0) { "不支持的日支：$branch" }
        require(stemIndex % 2 == branchIndex % 2) { "日辰干支不合法：${stem}${branch}" }
        // 按日干支所属旬推空亡
        val adjustedBranchIndex = if (branchIndex <= stemIndex) {
            branchIndex + EarthlyBranches.size
        } else {
            branchIndex
        }
        val xunKongIndex = (adjustedBranchIndex - stemIndex) / 2 - 1
        require(xunKongIndex in XunKongBranches.indices) { "日辰干支不合法：${stem}${branch}" }
        return XunKongBranches[xunKongIndex]
    }

    private fun resolveShiYing(mark: String): Pair<Int, Int> {
        val outer = mark.takeLast(3)
        val inner = mark.take(3)

        if (outer[2] == inner[2]) {
            if (outer[1] != inner[1] && outer[0] != inner[0]) {
                return 2 to 5
            }
        } else if (outer[1] == inner[1] && outer[0] == inner[0]) {
            return 5 to 2
        }

        if (outer[1] == inner[1]) {
            if (outer[0] != inner[0] && outer[2] != inner[2]) {
                return 4 to 1
            }
        } else if (outer[0] == inner[0] && outer[2] == inner[2]) {
            return 3 to 6
        }

        if (outer[0] == inner[0]) {
            if (outer[1] != inner[1] && outer[2] != inner[2]) {
                return 4 to 1
            }
        } else if (outer[1] == inner[1] && outer[2] == inner[2]) {
            return 1 to 4
        }

        if (outer == inner) {
            return 6 to 3
        }

        return 3 to 6
    }

    private fun resolvePalaceIndex(mark: String, shiLine: Int): Int {
        val outer = mark.takeLast(3)
        val inner = mark.take(3)
        val hexagramType = resolveSoulType(mark)
        return when {
            hexagramType == "归魂" -> TrigramMarks.indexOf(inner)
            shiLine in listOf(1, 2, 3, 6) -> TrigramMarks.indexOf(outer)
            shiLine in listOf(4, 5) || hexagramType == "游魂" -> {
                val invertedInner = inner.map { if (it == '1') '0' else '1' }.joinToString(separator = "")
                TrigramMarks.indexOf(invertedInner)
            }

            else -> TrigramMarks.indexOf(outer)
        }
    }

    private fun resolveSoulType(mark: String): String? {
        val outer = mark.takeLast(3)
        val inner = mark.take(3)
        return if (outer[1] == inner[1]) {
            if (outer[0] != inner[0] && outer[2] != inner[2]) {
                "游魂"
            } else {
                null
            }
        } else if (outer[0] == inner[0] && outer[2] == inner[2]) {
            "归魂"
        } else {
            null
        }
    }

    private fun resolveHexagramType(mark: String): String? {
        resolveSoulType(mark)?.let { return it }
        if (isSixClash(mark)) {
            return "六冲"
        }
        val name = HexagramNames.getValue(mark)
        return SixCombinationNames.firstOrNull { it in name }?.let { "六合" }
    }

    private fun isSixClash(mark: String): Boolean {
        val outer = mark.takeLast(3)
        val inner = mark.take(3)
        if (outer == inner) {
            return true
        }
        return setOf(inner, outer) == setOf("100", "111")
    }

    private fun resolveNaJia(mark: String): List<GanZhiValue> {
        val inner = TrigramMarks.indexOf(mark.take(3))
        val outer = TrigramMarks.indexOf(mark.takeLast(3))
        require(inner >= 0 && outer >= 0) { "不支持的卦码：$mark" }

        val innerNaJia = buildList {
            val (stem, branches) = PalaceNaJia[inner].first
            branches.forEach { branch ->
                add(GanZhiValue(stem = stem, branch = branch.toString()))
            }
        }
        val outerNaJia = buildList {
            val (stem, branches) = PalaceNaJia[outer].second
            branches.forEach { branch ->
                add(GanZhiValue(stem = stem, branch = branch.toString()))
            }
        }
        return innerNaJia + outerNaJia
    }

    private fun resolveSixRelative(
        palaceElement: String,
        branchElement: String
    ): String {
        val palaceIndex = FiveElements.indexOf(palaceElement)
        val branchIndex = FiveElements.indexOf(branchElement)
        val relativeIndex = (palaceIndex - branchIndex).let { if (it < 0) it + 5 else it }
        return SixRelativeSequence[relativeIndex]
    }

    private fun resolveHiddenLines(palaceIndex: Int): List<HiddenLineValue> {
        // 先取本宫纯卦六爻，结果页再按设置决定显示全部藏爻还是只显示缺失六亲
        val purePalaceMark = TrigramMarks[palaceIndex] + TrigramMarks[palaceIndex]
        val pureNaJia = resolveNaJia(purePalaceMark)
        val pureRelatives = pureNaJia.map { value ->
            resolveSixRelative(
                palaceElement = PalaceElements[palaceIndex],
                branchElement = branchToElement(value.branch)
            )
        }
        return List(6) { index ->
            val ganZhi = pureNaJia[index]
            HiddenLineValue(
                relative = pureRelatives[index],
                stem = ganZhi.stem,
                branch = ganZhi.branch,
                element = branchToElement(ganZhi.branch)
            )
        }
    }

    private fun branchToElement(branch: String): String {
        val branchIndex = EarthlyBranches.indexOf(branch)
        require(branchIndex >= 0) { "不支持的地支：$branch" }
        return BranchElements[branchIndex]
    }
}

private fun DivinationLine.toBaseLineSymbol(): String {
    return if (isYang) {
        YangLineSymbol
    } else {
        YinLineSymbol
    }
}

private fun Char.toLineSymbol(): String {
    return if (this == '1') {
        YangLineSymbol
    } else {
        YinLineSymbol
    }
}
