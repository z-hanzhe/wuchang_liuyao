package site.hanzhe.wuchang_liuyao.domain.divination

private val EarthlyBranchClashPairs = mapOf(
    "子" to "午",
    "丑" to "未",
    "寅" to "申",
    "卯" to "酉",
    "辰" to "戌",
    "巳" to "亥",
    "午" to "子",
    "未" to "丑",
    "申" to "寅",
    "酉" to "卯",
    "戌" to "辰",
    "亥" to "巳"
)

private val EarthlyBranchCombinePairs = mapOf(
    "子" to "丑",
    "丑" to "子",
    "寅" to "亥",
    "卯" to "戌",
    "辰" to "酉",
    "巳" to "申",
    "午" to "未",
    "未" to "午",
    "申" to "巳",
    "酉" to "辰",
    "戌" to "卯",
    "亥" to "寅"
)

private val FiveElementGenerateMap = mapOf(
    "木" to "火",
    "火" to "土",
    "土" to "金",
    "金" to "水",
    "水" to "木"
)

private val FiveElementControlMap = mapOf(
    "木" to "土",
    "火" to "金",
    "土" to "水",
    "金" to "木",
    "水" to "火"
)

private val EarthlyBranchSet = setOf(
    "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
)

internal enum class BranchHighlightType {
    NONE,
    SAME,
    CLASH,
    COMBINE
}

internal data class DivinationTableRowHints(
    val hiddenPromptTags: List<String> = emptyList(),
    val basePromptTags: List<String> = emptyList(),
    val changedPromptTags: List<String> = emptyList(),
    val hiddenXunKong: Boolean = false,
    val baseXunKong: Boolean = false,
    val changedXunKong: Boolean = false
)

internal fun buildDivinationTableRowHints(
    result: DivinationResult,
    row: DivinationTableRow
): DivinationTableRowHints {
    val monthBranch = result.dateInfo.month.earthlyBranch.orEmpty()
    val dayBranch = result.dateInfo.day.earthlyBranch.orEmpty()
    val xunKongBranches = result.dayXunKong.map { it.toString() }.toSet()
    return DivinationTableRowHints(
        hiddenPromptTags = buildDayMonthPromptTags(
            branch = row.hiddenBranch,
            monthBranch = monthBranch,
            dayBranch = dayBranch
        ),
        basePromptTags = buildDayMonthPromptTags(
            branch = row.baseBranch,
            monthBranch = monthBranch,
            dayBranch = dayBranch
        ),
        changedPromptTags = buildDayMonthPromptTags(
            branch = row.changedBranch,
            monthBranch = monthBranch,
            dayBranch = dayBranch
        ) + buildHuiTouPromptTags(row),
        hiddenXunKong = row.hiddenBranch in xunKongBranches,
        baseXunKong = row.baseBranch in xunKongBranches,
        changedXunKong = row.changedBranch in xunKongBranches
    )
}

private fun buildDayMonthPromptTags(
    branch: String,
    monthBranch: String,
    dayBranch: String
): List<String> {
    if (branch.isBlank()) {
        return emptyList()
    }
    return buildList {
        if (isEarthlyBranchClash(branch, monthBranch)) {
            add("月破")
        }
        if (isEarthlyBranchCombine(branch, monthBranch)) {
            add("月合")
        }
        if (isEarthlyBranchClash(branch, dayBranch)) {
            add("日冲")
        }
        if (isEarthlyBranchCombine(branch, dayBranch)) {
            add("日合")
        }
    }
}

private fun buildHuiTouPromptTags(row: DivinationTableRow): List<String> {
    if (row.changingSymbol.isBlank() || row.baseBranch.isBlank() || row.changedBranch.isBlank()) {
        return emptyList()
    }
    return buildList {
        if (isFiveElementGenerate(row.changedElement, row.baseElement)) {
            add("回头生")
        }
        if (isFiveElementControl(row.changedElement, row.baseElement)) {
            add("回头克")
        }
        if (isEarthlyBranchClash(row.changedBranch, row.baseBranch)) {
            add("回头冲")
        }
        if (isEarthlyBranchCombine(row.changedBranch, row.baseBranch)) {
            add("回头合")
        }
    }
}

private fun isEarthlyBranchClash(left: String, right: String): Boolean {
    if (left.isBlank() || right.isBlank()) {
        return false
    }
    return EarthlyBranchClashPairs[left] == right
}

private fun isEarthlyBranchCombine(left: String, right: String): Boolean {
    if (left.isBlank() || right.isBlank()) {
        return false
    }
    return EarthlyBranchCombinePairs[left] == right
}

private fun isFiveElementGenerate(left: String, right: String): Boolean {
    if (left.isBlank() || right.isBlank()) {
        return false
    }
    return FiveElementGenerateMap[left] == right
}

private fun isFiveElementControl(left: String, right: String): Boolean {
    if (left.isBlank() || right.isBlank()) {
        return false
    }
    return FiveElementControlMap[left] == right
}

internal fun resolveBranchHighlightType(
    selectedBranch: String?,
    branch: String
): BranchHighlightType {
    if (selectedBranch.isNullOrBlank() || branch.isBlank()) {
        return BranchHighlightType.NONE
    }
    return when {
        branch == selectedBranch -> BranchHighlightType.SAME
        isEarthlyBranchClash(branch, selectedBranch) -> BranchHighlightType.CLASH
        isEarthlyBranchCombine(branch, selectedBranch) -> BranchHighlightType.COMBINE
        else -> BranchHighlightType.NONE
    }
}

internal fun resolveBranchesHighlightType(
    selectedBranch: String?,
    branchText: String
): BranchHighlightType {
    if (selectedBranch.isNullOrBlank() || branchText.isBlank()) {
        return BranchHighlightType.NONE
    }
    val branches = branchText.map { it.toString() }.filter { it in EarthlyBranchSet }
    if (branches.isEmpty()) {
        return BranchHighlightType.NONE
    }
    if (branches.any { it == selectedBranch }) {
        return BranchHighlightType.SAME
    }
    if (branches.any { isEarthlyBranchClash(it, selectedBranch) }) {
        return BranchHighlightType.CLASH
    }
    if (branches.any { isEarthlyBranchCombine(it, selectedBranch) }) {
        return BranchHighlightType.COMBINE
    }
    return BranchHighlightType.NONE
}
