package site.hanzhe.wuchang_liuyao.feature.home

import kotlin.random.Random

internal const val UnselectedYaoValue = "---- 未选择 ----"

internal val YaoNames = listOf("上爻：", "五爻：", "四爻：", "三爻：", "二爻：", "初爻：")
internal val YaoValueOptions = listOf(
    YaoValueOption(label = "少阳", lineType = YaoLineType.YANG),
    YaoValueOption(label = "少阴", lineType = YaoLineType.YIN),
    YaoValueOption(label = "老阳", lineType = YaoLineType.YANG, marker = "O"),
    YaoValueOption(label = "老阴", lineType = YaoLineType.YIN, marker = "X")
)
internal val CoinValueOptions = listOf("背 正 正", "背 背 正", "背 背 背", "正 正 正")
internal val PointSelectionYaoNames = YaoNames

// 存储顺序按界面展示顺序保存，即上爻到初爻
internal fun emptySelectedValues(): List<String> = List(YaoNames.size) { UnselectedYaoValue }
internal fun emptyOnlineShakeValues(): List<String> = List(YaoNames.size) { "" }
internal fun debugYaoSelectedValues(): List<String> = listOf("老阳", "老阳", "老阴", "老阴", "老阳", "老阳")
// 点选起卦内部仍按上爻到初爻保存，界面显示时再反转
internal fun defaultPointSelectionLines(): List<PointSelectionLineState> =
    List(YaoNames.size) { PointSelectionLineState() }
internal fun defaultOnlineShakeCoinFaces(): List<OnlineShakeCoinFace> =
    List(3) { OnlineShakeCoinFace.FRONT }
internal fun randomOnlineShakeCoinFaces(): List<OnlineShakeCoinFace> =
    List(3) {
        if (Random.nextBoolean()) {
            OnlineShakeCoinFace.FRONT
        } else {
            OnlineShakeCoinFace.BACK
        }
    }

internal enum class YaoLineType {
    YANG,
    YIN
}

internal data class YaoValueOption(
    val label: String,
    val lineType: YaoLineType,
    val marker: String? = null
)

internal data class PointSelectionLineState(
    val isYang: Boolean = false,
    val isMoving: Boolean = false
)

internal enum class OnlineShakeCoinFace(val displayText: String) {
    FRONT("正"),
    BACK("背")
}

internal fun resolveOnlineShakeYaoValue(coinFaces: List<OnlineShakeCoinFace>): String {
    return when (coinFaces.count { it == OnlineShakeCoinFace.BACK }) {
        3 -> "老阳"
        2 -> "少阴"
        1 -> "少阳"
        else -> "老阴"
    }
}

internal fun findYaoValueOption(value: String): YaoValueOption? {
    return YaoValueOptions.firstOrNull { it.label == value }
}

internal enum class TrigramOption(
    val displayText: String,
    val linesTopDown: String
) {
    QIAN("☰ 乾卦", "111"),
    ZHEN("☳ 震卦", "001"),
    KAN("☵ 坎卦", "010"),
    GEN("☶ 艮卦", "100"),
    XUN("☴ 巽卦", "110"),
    LI("☲ 离卦", "101"),
    DUI("☱ 兑卦", "011"),
    KUN("☷ 坤卦", "000")
}

internal enum class HexagramTrigramField {
    BASE_UPPER,
    BASE_LOWER,
    CHANGED_UPPER,
    CHANGED_LOWER
}

internal data class HexagramNameSelectionState(
    val baseUpper: TrigramOption? = null,
    val baseLower: TrigramOption? = null,
    val changedUpper: TrigramOption? = null,
    val changedLower: TrigramOption? = null
) {
    /** 获取指定位置当前选择的八卦。 */
    fun selectionOf(field: HexagramTrigramField): TrigramOption? {
        return when (field) {
            HexagramTrigramField.BASE_UPPER -> baseUpper
            HexagramTrigramField.BASE_LOWER -> baseLower
            HexagramTrigramField.CHANGED_UPPER -> changedUpper
            HexagramTrigramField.CHANGED_LOWER -> changedLower
        }
    }

    /** 更新指定的上下卦选择。 */
    fun withSelection(field: HexagramTrigramField, trigram: TrigramOption): HexagramNameSelectionState {
        return when (field) {
            HexagramTrigramField.BASE_UPPER -> copy(baseUpper = trigram)
            HexagramTrigramField.BASE_LOWER -> copy(baseLower = trigram)
            HexagramTrigramField.CHANGED_UPPER -> copy(changedUpper = trigram)
            HexagramTrigramField.CHANGED_LOWER -> copy(changedLower = trigram)
        }
    }
}

internal enum class InputSectionType {
    YAO_NAME,
    HEXAGRAM_NAME,
    COIN,
    POINT_SELECT,
    ONLINE_SHAKE
}

internal enum class DivinationMethod(
    val label: String,
    val inputSectionType: InputSectionType?
) {
    YAO_NAME("爻名起卦", InputSectionType.YAO_NAME),
    HEXAGRAM_NAME("卦名起卦", InputSectionType.HEXAGRAM_NAME),
    POINT_SELECT("点选起卦", InputSectionType.POINT_SELECT),
    COIN("铜钱摇卦", InputSectionType.COIN),
    MANUAL("电脑摇卦", InputSectionType.ONLINE_SHAKE),
    AUTO("电脑起卦", null),
}

internal data class YaoPickerState(
    val sectionType: InputSectionType,
    val index: Int
)

internal data class HomeUiState(
    val dialogMessage: String? = null,
    val question: String = "",
    val selectedMethod: DivinationMethod = DivinationMethod.YAO_NAME,
    val selectedYaoValues: List<String> = emptySelectedValues(),
    val selectedCoinValues: List<String> = emptySelectedValues(),
    val hexagramNameSelection: HexagramNameSelectionState = HexagramNameSelectionState(),
    val pointSelectionLines: List<PointSelectionLineState> = defaultPointSelectionLines(),
    val onlineShakeValues: List<String> = emptyOnlineShakeValues(),
    val onlineShakeCoinFaces: List<OnlineShakeCoinFace> = defaultOnlineShakeCoinFaces(),
    val nextOnlineShakeIndex: Int = YaoNames.lastIndex,
    val isOnlineShakeAnimating: Boolean = false,
    val isMethodSheetVisible: Boolean = false,
    val activeYaoPicker: YaoPickerState? = null
)

internal fun HomeUiState.canStartDivination(): Boolean {
    if (selectedMethod == DivinationMethod.AUTO) {
        return true
    }
    return when (selectedMethod.inputSectionType) {
        InputSectionType.YAO_NAME -> selectedYaoValues.all { it != UnselectedYaoValue }
        InputSectionType.HEXAGRAM_NAME -> {
            hexagramNameSelection.baseUpper != null && hexagramNameSelection.baseLower != null
        }
        InputSectionType.COIN -> selectedCoinValues.all { it != UnselectedYaoValue }
        InputSectionType.POINT_SELECT -> pointSelectionLines.size == PointSelectionYaoNames.size
        InputSectionType.ONLINE_SHAKE -> onlineShakeValues.all { it.isNotBlank() }
        null -> false
    }
}
