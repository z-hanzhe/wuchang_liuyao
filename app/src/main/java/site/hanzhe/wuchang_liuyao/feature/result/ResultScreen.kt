package site.hanzhe.wuchang_liuyao.feature.result

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.text.TextUtils
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import site.hanzhe.wuchang_liuyao.domain.divination.BranchHighlightType
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationDateInfo
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationResult
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationSpirit
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationTableRow
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationTableRowHints
import site.hanzhe.wuchang_liuyao.domain.divination.GanzhiPillar
import site.hanzhe.wuchang_liuyao.domain.divination.buildDivinationTableRowHints
import site.hanzhe.wuchang_liuyao.domain.divination.resolveBranchHighlightType
import site.hanzhe.wuchang_liuyao.domain.divination.resolveBranchesHighlightType
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.ui.common.WuchangDialogActionButton
import site.hanzhe.wuchang_liuyao.ui.common.TopBarTextAction
import site.hanzhe.wuchang_liuyao.ui.common.WuchangTopAppBar
import site.hanzhe.wuchang_liuyao.ui.common.noRippleClick
import site.hanzhe.wuchang_liuyao.ui.theme.AppTextHint
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

private val ResultColumnGap = 7.dp
private val ResultRowGap = 16.dp
private val ResultHintRowGap = 2.dp
private val ResultHeaderHeight = 28.dp
private val ResultCellVerticalPadding = 2.dp
private val ResultDataRowBottomPadding = 1.dp
private val ResultHeaderVerticalPadding = 4.dp
private val ResultHintRowHeight = 13.dp
private val ResultXunKongHorizontalPadding = 0.dp
private val ResultXunKongVerticalPadding = 0.dp
private val ResultLineFullWidth = 34.dp
private val ResultLineHalfWidth = 13.dp
private val ResultLineGap = 7.dp
private val ResultLineHeight = 7.dp
private val ResultEditInputCorner = 10.dp
private const val ResultMaxSpiritsPerRow = 5
private const val ResultFallbackSpiritsPerRow = 4
private const val ResultSpiritItemGap = "  "
private val ResultLunarClockTimeRegex = Regex("""\s+\d{1,2}时\d{1,2}分$""")

private data class ResultVisibleColumns(
    val hiddenGroup: Boolean,
    val changing: Boolean,
    val changedLine: Boolean,
    val changedGroup: Boolean
)

private data class ResultBranchSelection(
    val branch: String,
    val cellId: String
)

private data class ResultDisplayOptions(
    val compactSixGod: Boolean,
    val showHeavenlyStem: Boolean,
    val showAllHiddenLines: Boolean,
    val compactSixRelative: Boolean,
    val showHuiTouShengKeHint: Boolean,
    val showHuiTouChongHeHint: Boolean,
    val showDayMonthChongHeHint: Boolean,
    val markBranchXunKong: Boolean
)

private data class ResultGroupContent(
    val relativeText: String,
    val stemText: String,
    val branchText: String,
    val elementText: String,
    val branchHighlighted: Boolean = false
) {
    fun hasContent(): Boolean {
        return relativeText.isNotBlank() ||
            stemText.isNotBlank() ||
            branchText.isNotBlank() ||
            elementText.isNotBlank()
    }
}

private data class ResultTableViewStyle(
    val viewportWidthPx: Int,
    val textColor: Int,
    val lineColor: Int,
    val hintTextColor: Int,
    val xunKongBackgroundColor: Int,
    val sameBranchBackgroundColor: Int,
    val clashBranchBackgroundColor: Int,
    val combineBranchBackgroundColor: Int,
    val sameBranchTextColor: Int,
    val clashBranchTextColor: Int,
    val combineBranchTextColor: Int,
    val textSizePx: Float,
    val hintTextSizePx: Float,
    val cellGapPx: Int,
    val rowGapPx: Int,
    val hintRowGapPx: Int,
    val cellVerticalPaddingPx: Int,
    val dataRowBottomPaddingPx: Int,
    val headerHeightPx: Int,
    val headerVerticalPaddingPx: Int,
    val hintRowMinHeightPx: Int,
    val xunKongHorizontalPaddingPx: Int,
    val xunKongVerticalPaddingPx: Int,
    val lineWidthPx: Int,
    val lineHalfWidthPx: Int,
    val lineGapPx: Int,
    val lineHeightPx: Int
)

private data class ResultHintRowContent(
    val hiddenText: String,
    val baseText: String,
    val changedText: String
)

private data class ResultBranchHighlightPalette(
    val sameBackground: Color,
    val clashBackground: Color,
    val combineBackground: Color,
    val sameText: Color,
    val clashText: Color,
    val combineText: Color
)

@Composable
internal fun ResultScreen(
    result: DivinationResult,
    compactSixGod: Boolean,
    showHeavenlyStem: Boolean,
    showAllHiddenLines: Boolean,
    compactSixRelative: Boolean,
    showHuiTouShengKeHint: Boolean,
    showHuiTouChongHeHint: Boolean,
    showDayMonthChongHeHint: Boolean,
    markBranchXunKong: Boolean,
    clickHighlightHint: Boolean,
    currentSituation: String,
    judgment: String,
    showSaveAction: Boolean,
    showEditAction: Boolean,
    editDraft: ResultEditDraft?,
    onSaveClick: () -> Unit,
    onEditClick: () -> Unit,
    onEditQuestionChange: (String) -> Unit,
    onEditCurrentSituationChange: (String) -> Unit,
    onEditJudgmentChange: (String) -> Unit,
    onEditDismiss: () -> Unit,
    onEditSave: () -> Unit,
    onBackClick: () -> Unit
) {
    val verticalScrollState = rememberScrollState()
    var branchSelection by remember(result) { mutableStateOf<ResultBranchSelection?>(null) }
    val displayOptions = ResultDisplayOptions(
        compactSixGod = compactSixGod,
        showHeavenlyStem = showHeavenlyStem,
        showAllHiddenLines = showAllHiddenLines,
        compactSixRelative = compactSixRelative,
        showHuiTouShengKeHint = showHuiTouShengKeHint,
        showHuiTouChongHeHint = showHuiTouChongHeHint,
        showDayMonthChongHeHint = showDayMonthChongHeHint,
        markBranchXunKong = markBranchXunKong
    )
    if (!clickHighlightHint && branchSelection != null) {
        branchSelection = null
    }
    BackHandler(onBack = onBackClick)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ResultTopAppBar(
                showSaveAction = showSaveAction,
                showEditAction = showEditAction,
                onSaveClick = onSaveClick,
                onEditClick = onEditClick,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .noRippleClick {
                    if (clickHighlightHint) {
                        branchSelection = null
                    }
                }
                .verticalScroll(verticalScrollState)
                .padding(
                    start = 12.dp,
                    top = innerPadding.calculateTopPadding() + 18.dp,
                    end = 12.dp,
                    bottom = innerPadding.calculateBottomPadding() +
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                        14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            ResultQuestionText(question = result.question)
            ResultMethodText(methodLabel = result.methodLabel)
            if (currentSituation.isNotBlank()) {
                ResultSupplementText(
                    label = "现状：",
                    value = currentSituation
                )
            }
            if (judgment.isNotBlank()) {
                ResultSupplementText(
                    label = "断语：",
                    value = judgment
                )
            }
            ResultSpiritsPanel(
                spirits = result.spirits,
                selectedBranch = if (clickHighlightHint) branchSelection?.branch else null
            )
            ResultTimePanel(
                result = result
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    ResultGridTable(
                        result = result,
                        displayOptions = displayOptions,
                        viewportWidth = maxWidth,
                        branchSelection = if (clickHighlightHint) branchSelection else null,
                        onBranchSelectionChange = { branchSelection = it },
                        clickHighlightEnabled = clickHighlightHint
                    )
                }
            }
        }
    }

    editDraft?.let { draft ->
        ResultEditDialog(
            draft = draft,
            onQuestionChange = onEditQuestionChange,
            onCurrentSituationChange = onEditCurrentSituationChange,
            onJudgmentChange = onEditJudgmentChange,
            onDismiss = onEditDismiss,
            onSave = onEditSave
        )
    }
}

@Composable
private fun ResultTopAppBar(
    showSaveAction: Boolean,
    showEditAction: Boolean,
    onSaveClick: () -> Unit,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit
) {
    WuchangTopAppBar(
        title = "无常六爻排盘",
        onBackClick = onBackClick,
        actions = {
            if (showSaveAction) {
                TopBarTextAction(
                    text = "保存",
                    onClick = onSaveClick
                )
            } else if (showEditAction) {
                TopBarTextAction(
                    text = "编辑",
                    onClick = onEditClick
                )
            }
        }
    )
}

@Composable
private fun ResultQuestionText(question: String) {
    Text(
        text = "问念：${question.ifBlank { "无" }}",
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Medium
    )
}

/** 使用与问念一致的样式展示起卦方式。 */
@Composable
private fun ResultMethodText(methodLabel: String) {
    // 手动选择的起卦方式统一显示为手工指定，兼容历史记录中的原始名称。
    val displayMethodLabel = when (methodLabel) {
        "爻名起卦", "卦名起卦", "点选起卦", "铜钱摇卦" -> "手工指定"
        else -> methodLabel
    }
    Text(
        text = "起卦方式：$displayMethodLabel",
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ResultSupplementText(
    label: String,
    value: String
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(label)
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                append(value)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}

@Composable
private fun ResultSpiritsPanel(
    spirits: List<DivinationSpirit>,
    selectedBranch: String?
) {
    val palette = rememberResultBranchHighlightPalette()
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textStyle = TextStyle(
        fontSize = 14.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val availableWidthPx = with(density) { maxWidth.roundToPx() }
        val spiritsPerRow = resolveSpiritsPerRow(
            spirits = spirits,
            availableWidthPx = availableWidthPx,
            textMeasurer = textMeasurer,
            textStyle = textStyle
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            spirits.chunked(spiritsPerRow).forEach { row ->
                Text(
                    text = buildSpiritsRowText(
                        spirits = row,
                        selectedBranch = selectedBranch,
                        palette = palette
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = textStyle
                )
            }
        }
    }
}

@Composable
private fun ResultTimePanel(
    result: DivinationResult
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ResultTimeTextRow(label = "公历", value = result.dateInfo.solarText.removePrefix("公历 "))
        ResultTimeTextRow(label = "农历", value = buildResultLunarTimeText(result.dateInfo))
        ResultGanzhiTimeRow(
            dateInfo = result.dateInfo,
            dayXunKong = result.dayXunKong
        )
    }
}

@Composable
private fun ResultEditDialog(
    draft: ResultEditDraft,
    onQuestionChange: (String) -> Unit,
    onCurrentSituationChange: (String) -> Unit,
    onJudgmentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ResultEditField(
                    title = "问念"
                ) {
                    ResultSingleLineInput(
                        value = draft.question,
                        onValueChange = onQuestionChange,
                        placeholder = "请输入问念"
                    )
                }
                ResultEditField(
                    title = "现状"
                ) {
                    ResultMultiLineInput(
                        value = draft.currentSituation,
                        onValueChange = onCurrentSituationChange,
                        placeholder = "可补充现状对轨信息"
                    )
                }
                ResultEditField(
                    title = "断语"
                ) {
                    ResultMultiLineInput(
                        value = draft.judgment,
                        onValueChange = onJudgmentChange,
                        placeholder = "在这里添加断语"
                    )
                }
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WuchangDialogActionButton(
                        modifier = Modifier.weight(1f),
                        text = "取消",
                        isPrimary = false,
                        onClick = onDismiss
                    )
                    WuchangDialogActionButton(
                        modifier = Modifier.weight(1f),
                        text = "保存",
                        isPrimary = true,
                        onClick = onSave
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultEditField(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
private fun ResultSingleLineInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ResultEditInputCorner))
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(ResultEditInputCorner)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        color = AppTextHint,
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun ResultMultiLineInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .clip(RoundedCornerShape(ResultEditInputCorner))
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(ResultEditInputCorner)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        color = AppTextHint,
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun ResultTimeTextRow(
    label: String,
    value: String
) {
    Text(
        text = "$label：$value",
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ResultGanzhiTimeRow(
    dateInfo: DivinationDateInfo,
    dayXunKong: String
) {
    val highlightColor = MaterialTheme.colorScheme.error
    Text(
        text = buildGanzhiTimeText(
            dateInfo = dateInfo,
            dayXunKong = dayXunKong,
            highlightColor = highlightColor
        ),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ResultGridTable(
    result: DivinationResult,
    displayOptions: ResultDisplayOptions,
    viewportWidth: Dp,
    branchSelection: ResultBranchSelection?,
    onBranchSelectionChange: (ResultBranchSelection?) -> Unit,
    clickHighlightEnabled: Boolean
) {
    val palaceText = buildPalaceHeaderText(
        palaceName = result.palaceName,
        displayOptions = displayOptions
    )
    val baseText = buildHexagramNameHeaderText(
        hexagramName = result.baseHexagramName,
        hexagramType = result.hexagramType
    )
    val changedText = buildChangedHexagramHeaderText(
        hexagramName = result.changedHexagramName,
        hexagramType = result.changedHexagramType,
        displayOptions = displayOptions
    )
    val density = LocalDensity.current
    val visibleColumns = remember(result.rowsTopDown, displayOptions) {
        result.rowsTopDown.resolveVisibleColumns(displayOptions)
    }
    val highlightPalette = rememberResultBranchHighlightPalette()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val hintColor = MaterialTheme.colorScheme.error.toArgb()
    val xunKongBackgroundColor = Color(0xFF9BAEC2)
        .copy(alpha = 0.42f)
        .compositeOver(MaterialTheme.colorScheme.surface)
        .toArgb()
    val tableStyle = with(density) {
        val textSizePx = 14.sp.toPx()
        val hintTextSizePx = 9.sp.toPx()
        val lineScale = density.fontScale.let { fontScale ->
            if (fontScale >= 1f) {
                1f + (fontScale - 1f) * 1.35f
            } else {
                1f - (1f - fontScale) * 0.75f
            }
        }.coerceIn(0.78f, 1.72f)
        ResultTableViewStyle(
            viewportWidthPx = viewportWidth.roundToPx(),
            textColor = onSurfaceColor,
            lineColor = onSurfaceColor,
            hintTextColor = hintColor,
            xunKongBackgroundColor = xunKongBackgroundColor,
            sameBranchBackgroundColor = highlightPalette.sameBackground.toArgb(),
            clashBranchBackgroundColor = highlightPalette.clashBackground.toArgb(),
            combineBranchBackgroundColor = highlightPalette.combineBackground.toArgb(),
            sameBranchTextColor = highlightPalette.sameText.toArgb(),
            clashBranchTextColor = highlightPalette.clashText.toArgb(),
            combineBranchTextColor = highlightPalette.combineText.toArgb(),
            textSizePx = textSizePx,
            hintTextSizePx = hintTextSizePx,
            cellGapPx = ResultColumnGap.roundToPx(),
            rowGapPx = ResultRowGap.roundToPx(),
            hintRowGapPx = ResultHintRowGap.roundToPx(),
            cellVerticalPaddingPx = ResultCellVerticalPadding.roundToPx(),
            dataRowBottomPaddingPx = ResultDataRowBottomPadding.roundToPx(),
            headerHeightPx = maxOf(
                ResultHeaderHeight.roundToPx(),
                (textSizePx * 1.65f).toInt()
            ),
            headerVerticalPaddingPx = ResultHeaderVerticalPadding.roundToPx(),
            hintRowMinHeightPx = maxOf(
                ResultHintRowHeight.roundToPx(),
                (hintTextSizePx * 1.5f).toInt()
            ),
            xunKongHorizontalPaddingPx = ResultXunKongHorizontalPadding.roundToPx(),
            xunKongVerticalPaddingPx = ResultXunKongVerticalPadding.roundToPx(),
            lineWidthPx = (ResultLineFullWidth.roundToPx() * lineScale).toInt(),
            lineHalfWidthPx = (ResultLineHalfWidth.roundToPx() * lineScale).toInt(),
            lineGapPx = (ResultLineGap.roundToPx() * lineScale).toInt(),
            lineHeightPx = (ResultLineHeight.roundToPx() * lineScale).toInt()
        )
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = ::createResultTableScrollView,
        update = { scrollView ->
            bindResultTable(
                scrollView = scrollView,
                result = result,
                displayOptions = displayOptions,
                visibleColumns = visibleColumns,
                palaceText = palaceText,
                baseText = baseText,
                changedText = changedText,
                style = tableStyle,
                branchSelection = branchSelection,
                onBranchSelectionChange = onBranchSelectionChange,
                clickHighlightEnabled = clickHighlightEnabled
            )
        }
    )
}

private fun createResultTableScrollView(context: Context): HorizontalScrollView {
    return HorizontalScrollView(context).apply {
        isHorizontalScrollBarEnabled = false
        isFillViewport = true
        overScrollMode = View.OVER_SCROLL_NEVER
        addView(
            FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        )
    }
}

private fun bindResultTable(
    scrollView: HorizontalScrollView,
    result: DivinationResult,
    displayOptions: ResultDisplayOptions,
    visibleColumns: ResultVisibleColumns,
    palaceText: String,
    baseText: String,
    changedText: String,
    style: ResultTableViewStyle,
    branchSelection: ResultBranchSelection?,
    onBranchSelectionChange: (ResultBranchSelection?) -> Unit,
    clickHighlightEnabled: Boolean
) {
    val host = scrollView.getChildAt(0) as FrameLayout
    host.minimumWidth = style.viewportWidthPx
    host.setOnClickListener {
        if (clickHighlightEnabled) {
            onBranchSelectionChange(null)
        }
    }
    host.removeAllViews()
    host.addView(
        buildResultTableView(
            context = scrollView.context,
            result = result,
            displayOptions = displayOptions,
            visibleColumns = visibleColumns,
            palaceText = palaceText,
            baseText = baseText,
            changedText = changedText,
            style = style,
            branchSelection = branchSelection,
            onBranchSelectionChange = onBranchSelectionChange,
            clickHighlightEnabled = clickHighlightEnabled
        ),
        FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER_HORIZONTAL
        )
    )
}

private fun buildResultTableView(
    context: Context,
    result: DivinationResult,
    displayOptions: ResultDisplayOptions,
    visibleColumns: ResultVisibleColumns,
    palaceText: String,
    baseText: String,
    changedText: String,
    style: ResultTableViewStyle,
    branchSelection: ResultBranchSelection?,
    onBranchSelectionChange: (ResultBranchSelection?) -> Unit,
    clickHighlightEnabled: Boolean
): TableLayout {
    val baseRelativeSet = result.rowsTopDown.map { it.baseRelative }.toSet()

    // 用原生 TableLayout 统一测量列宽，避免 Compose 手写表格的列宽漂移
    return TableLayout(context).apply {
        isShrinkAllColumns = false
        isStretchAllColumns = false

        addView(
            buildResultHeaderRow(
                context = context,
                palaceText = palaceText,
                baseText = baseText,
                changedText = changedText,
                visibleColumns = visibleColumns,
                style = style
            ),
            createResultTableRowLayoutParams(style.rowGapPx)
        )

        result.rowsTopDown.forEachIndexed { index, row ->
            val rowHints = buildDivinationTableRowHints(result, row)
            val hiddenCellHasContent = buildHiddenGroupContent(
                row = row,
                hiddenXunKong = rowHints.hiddenXunKong,
                displayOptions = displayOptions,
                baseRelativeSet = baseRelativeSet
            ).hasContent()
            addView(
                buildResultDataRow(
                    context = context,
                    rowIndex = index,
                    row = row,
                    rowHints = rowHints,
                    displayOptions = displayOptions,
                    baseRelativeSet = baseRelativeSet,
                    visibleColumns = visibleColumns,
                    style = style,
                    branchSelection = branchSelection,
                    onBranchSelectionChange = onBranchSelectionChange,
                    clickHighlightEnabled = clickHighlightEnabled
                ),
                createResultTableRowLayoutParams(0)
            )
            addView(
                buildResultHintRow(
                    context = context,
                    rowHints = rowHints,
                    hiddenCellHasContent = hiddenCellHasContent,
                    displayOptions = displayOptions,
                    visibleColumns = visibleColumns,
                    style = style
                ),
                createResultTableRowLayoutParams(
                    if (index == result.rowsTopDown.lastIndex) {
                        0
                    } else {
                        style.hintRowGapPx + (style.cellVerticalPaddingPx - style.dataRowBottomPaddingPx)
                    }
                )
            )
        }
    }
}

private fun buildResultHeaderRow(
    context: Context,
    palaceText: String,
    baseText: String,
    changedText: String,
    visibleColumns: ResultVisibleColumns,
    style: ResultTableViewStyle
): TableRow {
    val hasChangedColumns = visibleColumns.hasChangedColumns()
    return createResultTableRow(context).apply {
        addView(
            createResultTextCell(
                context = context,
                text = palaceText,
                style = style,
                isBold = true,
                minHeightPx = style.headerHeightPx,
                verticalPaddingPx = style.headerVerticalPaddingPx,
                truncateWhenOverflow = false
            ),
            createResultCellLayoutParams(
                span = if (visibleColumns.hiddenGroup) 2 else 1,
                endMarginPx = style.cellGapPx
            )
        )
        addView(
            createResultTextCell(
                context = context,
                text = baseText,
                style = style,
                isBold = true,
                minHeightPx = style.headerHeightPx,
                verticalPaddingPx = style.headerVerticalPaddingPx,
                truncateWhenOverflow = false
            ),
            createResultCellLayoutParams(
                span = 2,
                endMarginPx = if (hasChangedColumns) style.cellGapPx else 0
            )
        )
        val changedSpan = visibleColumns.changedHeaderSpan()
        if (changedSpan > 0) {
            addView(
                createResultTextCell(
                    context = context,
                    text = changedText,
                    style = style,
                    isBold = true,
                    minHeightPx = style.headerHeightPx,
                    verticalPaddingPx = style.headerVerticalPaddingPx,
                    truncateWhenOverflow = false
                ),
                createResultCellLayoutParams(span = changedSpan)
            )
        }
    }
}

private fun buildResultDataRow(
    context: Context,
    rowIndex: Int,
    row: DivinationTableRow,
    rowHints: DivinationTableRowHints,
    displayOptions: ResultDisplayOptions,
    baseRelativeSet: Set<String>,
    visibleColumns: ResultVisibleColumns,
    style: ResultTableViewStyle,
    branchSelection: ResultBranchSelection?,
    onBranchSelectionChange: (ResultBranchSelection?) -> Unit,
    clickHighlightEnabled: Boolean
): TableRow {
    val hasChangedColumns = visibleColumns.changedLine || visibleColumns.changedGroup
    val hiddenCellId = "hidden_$rowIndex"
    val baseCellId = "base_$rowIndex"
    val changedCellId = "changed_$rowIndex"
    val hiddenGroupContent = buildHiddenGroupContent(
        row = row,
        hiddenXunKong = rowHints.hiddenXunKong,
        displayOptions = displayOptions,
        baseRelativeSet = baseRelativeSet
    )
    val baseGroupContent = buildGroupContent(
        relative = row.baseRelative,
        stem = row.baseStem,
        branch = row.baseBranch,
        element = row.baseElement,
        isBranchXunKong = rowHints.baseXunKong,
        displayOptions = displayOptions
    )
    val changedGroupContent = buildGroupContent(
        relative = row.changedRelative,
        stem = row.changedStem,
        branch = row.changedBranch,
        element = row.changedElement,
        isBranchXunKong = rowHints.changedXunKong,
        displayOptions = displayOptions
    )
    return createResultTableRow(context).apply {
        addView(
            createResultTextCell(
                context = context,
                text = formatSixGod(row.sixGod, displayOptions),
                style = style,
                bottomPaddingPx = style.dataRowBottomPaddingPx
            ),
            createResultCellLayoutParams(endMarginPx = style.cellGapPx)
        )
        if (visibleColumns.hiddenGroup) {
            addView(
                createResultGroupCell(
                    context = context,
                    content = hiddenGroupContent,
                    style = style,
                    highlightType = if (clickHighlightEnabled) {
                        resolveBranchHighlightType(branchSelection?.branch, row.hiddenBranch)
                    } else {
                        BranchHighlightType.NONE
                    },
                    isSelected = clickHighlightEnabled && branchSelection?.cellId == hiddenCellId,
                    onClick = if (
                        clickHighlightEnabled &&
                        hiddenGroupContent.hasContent() &&
                        row.hiddenBranch.isNotBlank()
                    ) {
                        {
                            onBranchSelectionChange(
                                branchSelection.toggleSelection(
                                    branch = row.hiddenBranch,
                                    cellId = hiddenCellId
                                )
                            )
                        }
                    } else {
                        null
                    }
                ),
                createResultCellLayoutParams(endMarginPx = style.cellGapPx)
            )
        }
        addView(
            createResultLineGroupCell(
                context = context,
                symbol = row.baseLineSymbol,
                content = baseGroupContent,
                style = style,
                highlightType = if (clickHighlightEnabled) {
                    resolveBranchHighlightType(branchSelection?.branch, row.baseBranch)
                } else {
                    BranchHighlightType.NONE
                },
                isSelected = clickHighlightEnabled && branchSelection?.cellId == baseCellId,
                onClick = if (clickHighlightEnabled) {
                    {
                        onBranchSelectionChange(
                            branchSelection.toggleSelection(
                                branch = row.baseBranch,
                                cellId = baseCellId
                            )
                        )
                    }
                } else {
                    null
                }
            ),
            createResultCellLayoutParams(endMarginPx = style.cellGapPx)
        )
        addView(
            createResultTextCell(
                    context = context,
                    text = row.shiYingMark,
                    style = style,
                    bottomPaddingPx = style.dataRowBottomPaddingPx
                ),
            createResultCellLayoutParams(
                endMarginPx = if (visibleColumns.changing || hasChangedColumns) style.cellGapPx else 0
            )
        )
        if (visibleColumns.changing) {
            addView(
                createResultTextCell(
                    context = context,
                    text = row.changingSymbol,
                    style = style,
                    bottomPaddingPx = style.dataRowBottomPaddingPx,
                    useMonospace = true
                ),
                createResultCellLayoutParams(
                    endMarginPx = if (hasChangedColumns) style.cellGapPx else 0
                )
            )
        }
        if (visibleColumns.changedLine || visibleColumns.changedGroup) {
            addView(
                createResultLineGroupCell(
                    context = context,
                    symbol = row.changedLineSymbol,
                    content = changedGroupContent,
                    style = style,
                    highlightType = if (clickHighlightEnabled) {
                        resolveBranchHighlightType(branchSelection?.branch, row.changedBranch)
                    } else {
                        BranchHighlightType.NONE
                    },
                    isSelected = clickHighlightEnabled && branchSelection?.cellId == changedCellId,
                    onClick = if (
                        clickHighlightEnabled &&
                        changedGroupContent.hasContent() &&
                        row.changedBranch.isNotBlank()
                    ) {
                        {
                            onBranchSelectionChange(
                                branchSelection.toggleSelection(
                                    branch = row.changedBranch,
                                    cellId = changedCellId
                                )
                            )
                        }
                    } else {
                        null
                    }
                ),
                createResultCellLayoutParams(
                    endMarginPx = 0
                )
            )
        }
    }
}

private fun buildResultHintRow(
    context: Context,
    rowHints: DivinationTableRowHints,
    hiddenCellHasContent: Boolean,
    displayOptions: ResultDisplayOptions,
    visibleColumns: ResultVisibleColumns,
    style: ResultTableViewStyle
): TableRow {
    val hasChangedColumns = visibleColumns.changedLine || visibleColumns.changedGroup
    val shouldMergeChangedHintWithChangingColumn = visibleColumns.changing && hasChangedColumns
    val hintRowContent = buildResultHintRowContent(
        rowHints = rowHints,
        hiddenCellHasContent = hiddenCellHasContent,
        displayOptions = displayOptions
    )
    return createResultTableRow(context).apply {
        addView(
            createResultHintCell(
                context = context,
                text = "",
                style = style,
                gravity = Gravity.START or Gravity.TOP
            ),
            createResultCellLayoutParams(endMarginPx = style.cellGapPx)
        )
        if (visibleColumns.hiddenGroup) {
            addView(
                createResultHintCell(
                    context = context,
                    text = hintRowContent.hiddenText,
                    style = style,
                    gravity = Gravity.START or Gravity.TOP
                ),
                createResultCellLayoutParams(endMarginPx = style.cellGapPx)
            )
        }
        addView(
            createResultHintCell(
                context = context,
                text = hintRowContent.baseText,
                style = style,
                gravity = Gravity.END or Gravity.TOP
            ),
            createResultCellLayoutParams(endMarginPx = style.cellGapPx)
        )
        addView(
            createResultHintCell(
                context = context,
                text = "",
                style = style,
                gravity = Gravity.START or Gravity.TOP
            ),
            createResultCellLayoutParams(
                endMarginPx = if (visibleColumns.changing || hasChangedColumns) style.cellGapPx else 0
            )
        )
        if (visibleColumns.changing && !shouldMergeChangedHintWithChangingColumn) {
            addView(
                createResultHintCell(
                    context = context,
                    text = "",
                    style = style,
                    gravity = Gravity.START or Gravity.TOP
                ),
                createResultCellLayoutParams(
                    endMarginPx = if (hasChangedColumns) style.cellGapPx else 0
                )
            )
        }
        if (visibleColumns.changedLine || visibleColumns.changedGroup) {
            addView(
                createResultHintCell(
                    context = context,
                    text = hintRowContent.changedText,
                    style = style,
                    gravity = Gravity.END or Gravity.TOP
                ),
                createResultCellLayoutParams(
                    // 红色提示小字行单独合并动爻列和变卦列，给右侧提示留出更宽空间
                    span = if (shouldMergeChangedHintWithChangingColumn) 2 else 1
                )
            )
        }
    }
}

private fun createResultTableRow(context: Context): TableRow {
    return TableRow(context).apply {
        gravity = Gravity.CENTER_VERTICAL
    }
}

private fun createResultTableRowLayoutParams(bottomMarginPx: Int): TableLayout.LayoutParams {
    return TableLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        bottomMargin = bottomMarginPx
    }
}

private fun createResultCellLayoutParams(
    span: Int = 1,
    endMarginPx: Int = 0
): TableRow.LayoutParams {
    return TableRow.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply {
        this.span = span
        rightMargin = endMarginPx
    }
}

private fun createResultTextCell(
    context: Context,
    text: String,
    style: ResultTableViewStyle,
    isBold: Boolean = true,
    useMonospace: Boolean = false,
    minHeightPx: Int = 0,
    verticalPaddingPx: Int = style.cellVerticalPaddingPx,
    bottomPaddingPx: Int = verticalPaddingPx,
    singleLine: Boolean = true,
    truncateWhenOverflow: Boolean = true,
    gravity: Int = Gravity.START or Gravity.CENTER_VERTICAL
): TextView {
    return TextView(context).apply {
        this.text = text
        setTextColor(style.textColor)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, style.textSizePx)
        includeFontPadding = false
        isSingleLine = singleLine
        ellipsize = if (singleLine && truncateWhenOverflow) TextUtils.TruncateAt.END else null
        setHorizontallyScrolling(singleLine)
        this.gravity = gravity
        setPadding(0, verticalPaddingPx, 0, bottomPaddingPx)
        if (minHeightPx > 0) {
            minimumHeight = minHeightPx
        }
        typeface = when {
            useMonospace && isBold -> Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            useMonospace -> Typeface.MONOSPACE
            isBold -> Typeface.DEFAULT_BOLD
            else -> Typeface.DEFAULT
        }
    }
}

private fun createResultHintCell(
    context: Context,
    text: String,
    style: ResultTableViewStyle,
    gravity: Int
): TextView {
    return TextView(context).apply {
        this.text = text
        setTextColor(style.hintTextColor)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, style.hintTextSizePx)
        includeFontPadding = false
        isSingleLine = true
        ellipsize = null
        setHorizontallyScrolling(false)
        minimumHeight = style.hintRowMinHeightPx
        this.gravity = gravity
    }
}

private fun createResultGroupCell(
    context: Context,
    content: ResultGroupContent,
    style: ResultTableViewStyle,
    highlightType: BranchHighlightType,
    isSelected: Boolean,
    onClick: (() -> Unit)? = null
): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        setPadding(0, style.cellVerticalPaddingPx, 0, style.dataRowBottomPaddingPx)
        if (content.hasContent()) {
            style.resolveBranchHighlightBackgroundColor(highlightType)?.let { backgroundColor ->
                setBackgroundColor(backgroundColor)
            }
        }
        onClick?.let {
            isClickable = true
            setOnClickListener { it() }
        }

        // 六亲、天干、地支、五行作为同一个表格单元格内的子内容渲染
        listOf(
            content.relativeText to false,
            content.stemText to false,
            content.branchText to content.branchHighlighted,
            content.elementText to false
        ).forEach { (segmentText, useXunKongBackground) ->
            if (segmentText.isBlank()) {
                return@forEach
            }
            addView(
                createResultSegmentTextCell(
                    context = context,
                    text = segmentText,
                    style = style,
                    highlightType = highlightType,
                    useXunKongBackground = useXunKongBackground,
                    isSelected = isSelected
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }
}

private fun createResultSegmentTextCell(
    context: Context,
    text: String,
    style: ResultTableViewStyle,
    highlightType: BranchHighlightType,
    useXunKongBackground: Boolean,
    isSelected: Boolean
): TextView {
    return createResultTextCell(
        context = context,
        text = text,
        style = style,
        verticalPaddingPx = 0
    ).apply {
        if (!useXunKongBackground) {
            style.resolveBranchHighlightTextColor(highlightType)?.let { highlightTextColor ->
                setTextColor(highlightTextColor)
            }
        }
        if (useXunKongBackground) {
            setPadding(
                style.xunKongHorizontalPaddingPx,
                style.xunKongVerticalPaddingPx,
                style.xunKongHorizontalPaddingPx,
                style.xunKongVerticalPaddingPx
            )
            setBackgroundColor(style.xunKongBackgroundColor)
        }
        if (isSelected) {
            typeface = Typeface.DEFAULT
        }
    }
}

private fun createResultLineGroupCell(
    context: Context,
    symbol: String,
    content: ResultGroupContent,
    style: ResultTableViewStyle,
    highlightType: BranchHighlightType,
    isSelected: Boolean,
    onClick: (() -> Unit)? = null
): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        onClick?.let {
            isClickable = true
            setOnClickListener { it() }
        }
        if (symbol.isNotBlank()) {
            addView(
                createResultLineCell(
                    context = context,
                    symbol = symbol,
                    style = style
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = if (content.hasContent()) style.cellGapPx else 0
                }
            )
        }
        if (content.hasContent()) {
            addView(
                createResultGroupCell(
                    context = context,
                    content = content,
                    style = style,
                    highlightType = highlightType,
                    isSelected = isSelected
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }
}

private fun createResultLineCell(
    context: Context,
    symbol: String,
    style: ResultTableViewStyle
): FrameLayout {
    return FrameLayout(context).apply {
        minimumWidth = style.lineWidthPx
        minimumHeight = style.lineHeightPx
        if (symbol.isBlank()) {
            return@apply
        }
        addView(
            if (isYinLineSymbol(symbol)) {
                createYinLineView(context, style)
            } else {
                createYangLineView(context, style)
            }
        )
    }
}

private fun createYangLineView(
    context: Context,
    style: ResultTableViewStyle
): View {
    return View(context).apply {
        setBackgroundColor(style.lineColor)
        layoutParams = FrameLayout.LayoutParams(
            style.lineWidthPx,
            style.lineHeightPx
        )
    }
}

private fun createYinLineView(
    context: Context,
    style: ResultTableViewStyle
): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(createLineSegmentView(context, style))
        addView(
            View(context).apply {
                layoutParams = LinearLayout.LayoutParams(style.lineGapPx, 1)
            }
        )
        addView(createLineSegmentView(context, style))
    }
}

private fun createLineSegmentView(
    context: Context,
    style: ResultTableViewStyle
): View {
    return View(context).apply {
        setBackgroundColor(style.lineColor)
        layoutParams = LinearLayout.LayoutParams(
            style.lineHalfWidthPx,
            style.lineHeightPx
        )
    }
}

private fun buildGroupContent(
    relative: String?,
    stem: String?,
    branch: String?,
    element: String?,
    displayOptions: ResultDisplayOptions,
    isBranchXunKong: Boolean = false
): ResultGroupContent {
    val relativeText = if (displayOptions.compactSixRelative) {
        formatSixRelative(relative)
    } else {
        relative.orEmpty()
    }
    val stemText = if (displayOptions.showHeavenlyStem) {
        stem.orEmpty()
    } else {
        ""
    }
    val branchText = branch.orEmpty()
    return ResultGroupContent(
        relativeText = relativeText,
        stemText = stemText,
        branchText = branchText,
        elementText = element.orEmpty(),
        branchHighlighted = displayOptions.markBranchXunKong && branchText.isNotBlank() && isBranchXunKong
    )
}

private fun buildHiddenGroupContent(
    row: DivinationTableRow,
    hiddenXunKong: Boolean = false,
    displayOptions: ResultDisplayOptions,
    baseRelativeSet: Set<String>
): ResultGroupContent {
    if (!displayOptions.showAllHiddenLines && row.hiddenRelative !in baseRelativeSet) {
        return buildGroupContent(
            relative = row.hiddenRelative,
            stem = row.hiddenStem,
            branch = row.hiddenBranch,
            element = row.hiddenElement,
            isBranchXunKong = hiddenXunKong,
            displayOptions = displayOptions
        )
    }
    if (displayOptions.showAllHiddenLines && !row.isHiddenSameAsBase(displayOptions)) {
        return buildGroupContent(
            relative = row.hiddenRelative,
            stem = row.hiddenStem,
            branch = row.hiddenBranch,
            element = row.hiddenElement,
            isBranchXunKong = hiddenXunKong,
            displayOptions = displayOptions
        )
    }
    return ResultGroupContent(
        relativeText = "",
        stemText = "",
        branchText = "",
        elementText = ""
    )
}

private fun buildResultHintRowContent(
    rowHints: DivinationTableRowHints,
    hiddenCellHasContent: Boolean,
    displayOptions: ResultDisplayOptions
): ResultHintRowContent {
    return ResultHintRowContent(
        hiddenText = if (hiddenCellHasContent) {
            buildResultHintText(
                tags = rowHints.hiddenPromptTags,
                displayOptions = displayOptions
            )
        } else {
            ""
        },
        baseText = buildResultHintText(
            tags = rowHints.basePromptTags,
            displayOptions = displayOptions
        ),
        changedText = buildResultHintText(
            tags = rowHints.changedPromptTags,
            displayOptions = displayOptions
        )
    )
}

private fun buildResultHintText(
    tags: List<String>,
    displayOptions: ResultDisplayOptions
): String {
    return tags.filter { tag ->
        when (tag) {
            "月破", "月合", "日冲", "日合" -> displayOptions.showDayMonthChongHeHint
            "回头生", "回头克" -> displayOptions.showHuiTouShengKeHint
            "回头冲", "回头合" -> displayOptions.showHuiTouChongHeHint
            else -> false
        }
    }.joinToString(separator = " ")
}

private fun formatSixGod(
    sixGod: String,
    displayOptions: ResultDisplayOptions
): String {
    if (!displayOptions.compactSixGod) {
        return sixGod
    }
    return when (sixGod) {
        "青龙" -> "龙"
        "玄武" -> "玄"
        "白虎" -> "虎"
        "螣蛇", "腾蛇" -> "蛇"
        "勾陈" -> "勾"
        "朱雀" -> "雀"
        else -> sixGod
    }
}

private fun formatSixRelative(relative: String?): String {
    return when (relative) {
        "父母" -> "父"
        "兄弟" -> "兄"
        "子孙" -> "孙"
        "妻财" -> "财"
        "官鬼" -> "官"
        else -> relative.orEmpty()
    }
}

private fun buildSpiritsRowText(
    spirits: List<DivinationSpirit>,
    selectedBranch: String?,
    palette: ResultBranchHighlightPalette
) = buildAnnotatedString {
    spirits.forEachIndexed { itemIndex, spirit ->
        val itemText = "${spirit.name}-${spirit.value}"
        val highlightType = resolveBranchesHighlightType(selectedBranch, spirit.value)
        withStyle(
            SpanStyle(
                color = palette.resolveTextColor(highlightType),
                background = palette.resolveBackground(highlightType)
            )
        ) {
            append(itemText)
        }
        if (itemIndex < spirits.lastIndex) {
            append(ResultSpiritItemGap)
        }
    }
}

private fun resolveSpiritsPerRow(
    spirits: List<DivinationSpirit>,
    availableWidthPx: Int,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle
): Int {
    if (spirits.size <= ResultFallbackSpiritsPerRow || availableWidthPx <= 0) {
        return ResultMaxSpiritsPerRow
    }
    val hasOverflowRow = spirits.chunked(ResultMaxSpiritsPerRow).any { row ->
        val rowText = row.joinToString(ResultSpiritItemGap) { spirit ->
            "${spirit.name}-${spirit.value}"
        }
        textMeasurer.measure(
            text = rowText,
            style = textStyle,
            maxLines = 1
        ).size.width > availableWidthPx
    }
    return if (hasOverflowRow) {
        ResultFallbackSpiritsPerRow
    } else {
        ResultMaxSpiritsPerRow
    }
}

private fun buildResultLunarTimeText(dateInfo: DivinationDateInfo): String {
    val lunarDateText = dateInfo.lunarText
        .removePrefix("农历 ")
        .replace(ResultLunarClockTimeRegex, "")
    val hourText = dateInfo.hour.text.takeIf {
        it.isNotBlank() && dateInfo.timeType != DivinationTimeType.GANZHI
    } ?: return lunarDateText
    val termText = dateInfo.termText.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
    return "$lunarDateText ${hourText}时$termText"
}

private fun buildGanzhiTimeText(
    dateInfo: DivinationDateInfo,
    dayXunKong: String,
    highlightColor: Color
) = buildAnnotatedString {
    append("干支：")
    var hasPillar = false

    fun appendPillar(
        pillar: GanzhiPillar,
        suffix: String,
        highlight: Boolean = false
    ) {
        val text = pillar.text.takeIf { it.isNotBlank() }?.plus(suffix) ?: return
        if (hasPillar) {
            append("  ")
        }
        if (highlight) {
            withStyle(
                SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text)
            }
        } else {
            append(text)
        }
        hasPillar = true
    }

    appendPillar(dateInfo.year, "年")
    appendPillar(dateInfo.month, "月", highlight = true)
    appendPillar(dateInfo.day, "日", highlight = true)
    if (dateInfo.timeType == DivinationTimeType.GANZHI) {
        appendPillar(dateInfo.hour, "时")
    }
    if (!hasPillar) {
        append(dateInfo.ganzhiText.removePrefix("干支 "))
    }
    withStyle(
        SpanStyle(
            color = highlightColor,
            fontWeight = FontWeight.Bold
        )
    ) {
        append("（")
        append(dayXunKong)
        append("）")
    }
}

@Composable
private fun rememberResultBranchHighlightPalette(): ResultBranchHighlightPalette {
    return ResultBranchHighlightPalette(
        sameBackground = Color(0xFFAFE0B3)
            .copy(alpha = 0.14f)
            .compositeOver(MaterialTheme.colorScheme.surface),
        clashBackground = Color(0xFFFFB9B3)
            .copy(alpha = 0.11f)
            .compositeOver(MaterialTheme.colorScheme.surface),
        combineBackground = Color(0xFFF0C36A)
            .copy(alpha = 0.17f)
            .compositeOver(MaterialTheme.colorScheme.surface),
        sameText = Color(0xFF365A39),
        clashText = Color(0xFF6C3F3A),
        combineText = Color(0xFF6C5830)
    )
}

private fun ResultBranchHighlightPalette.resolveBackground(
    highlightType: BranchHighlightType
): Color {
    return when (highlightType) {
        BranchHighlightType.SAME -> sameBackground
        BranchHighlightType.CLASH -> clashBackground
        BranchHighlightType.COMBINE -> combineBackground
        BranchHighlightType.NONE -> Color.Unspecified
    }
}

private fun ResultBranchHighlightPalette.resolveTextColor(
    highlightType: BranchHighlightType
): Color {
    return when (highlightType) {
        BranchHighlightType.SAME -> sameText
        BranchHighlightType.CLASH -> clashText
        BranchHighlightType.COMBINE -> combineText
        BranchHighlightType.NONE -> Color.Unspecified
    }
}

private fun ResultTableViewStyle.resolveBranchHighlightBackgroundColor(
    highlightType: BranchHighlightType
): Int? {
    return when (highlightType) {
        BranchHighlightType.SAME -> sameBranchBackgroundColor
        BranchHighlightType.CLASH -> clashBranchBackgroundColor
        BranchHighlightType.COMBINE -> combineBranchBackgroundColor
        BranchHighlightType.NONE -> null
    }
}

private fun ResultTableViewStyle.resolveBranchHighlightTextColor(
    highlightType: BranchHighlightType
): Int? {
    return when (highlightType) {
        BranchHighlightType.SAME -> sameBranchTextColor
        BranchHighlightType.CLASH -> clashBranchTextColor
        BranchHighlightType.COMBINE -> combineBranchTextColor
        BranchHighlightType.NONE -> null
    }
}

private fun ResultBranchSelection?.toggleSelection(
    branch: String,
    cellId: String
): ResultBranchSelection? {
    if (branch.isBlank()) {
        return this
    }
    return if (this?.cellId == cellId) {
        null
    } else {
        ResultBranchSelection(branch = branch, cellId = cellId)
    }
}

private fun buildPalaceHeaderText(
    palaceName: String?,
    displayOptions: ResultDisplayOptions
): String {
    val name = palaceName?.takeIf { it.isNotBlank() }.orEmpty()
    if (name.isBlank()) {
        return ""
    }
    return if (displayOptions.compactSixGod) name else "${name}宫"
}

private fun buildHexagramNameHeaderText(
    hexagramName: String?,
    hexagramType: String?
): String {
    val name = hexagramName?.takeIf { it.isNotBlank() }.orEmpty()
    val typeText = hexagramType?.takeIf { it.isNotBlank() }?.let { "($it)" }.orEmpty()
    return name + typeText
}

private fun buildChangedHexagramHeaderText(
    hexagramName: String?,
    hexagramType: String?,
    displayOptions: ResultDisplayOptions
): String {
    val text = buildHexagramNameHeaderText(
        hexagramName = hexagramName,
        hexagramType = hexagramType
    )
    // 仅在关闭显示天干、开启精简六亲且变卦文字超过 7 个字符时不补全角空格
    val shouldSkipLeadingFullWidthSpace = !displayOptions.showHeavenlyStem &&
        displayOptions.compactSixRelative &&
        text.length > 7
    return if (shouldSkipLeadingFullWidthSpace) {
        text
    } else {
        "　$text"
    }
}

private fun isYinLineSymbol(symbol: String): Boolean {
    return symbol.contains('　') || symbol.count { it == '━' } <= 2
}

private fun DivinationTableRow.isHiddenSameAsBase(
    displayOptions: ResultDisplayOptions
): Boolean {
    if (hiddenRelative != baseRelative) {
        return false
    }
    if (hiddenBranch != baseBranch || hiddenElement != baseElement) {
        return false
    }
    if (displayOptions.showHeavenlyStem && hiddenStem != baseStem) {
        return false
    }
    return true
}

private fun List<DivinationTableRow>.resolveVisibleColumns(
    displayOptions: ResultDisplayOptions
): ResultVisibleColumns {
    val baseRelativeSet = map { it.baseRelative }.toSet()
    return ResultVisibleColumns(
        hiddenGroup = any { row ->
            buildHiddenGroupContent(
                row = row,
                displayOptions = displayOptions,
                baseRelativeSet = baseRelativeSet
            ).hasContent()
        },
        changing = any { it.changingSymbol.isNotBlank() },
        changedLine = any { it.changedLineSymbol.isNotBlank() },
        changedGroup = any { row ->
            buildGroupContent(
                row.changedRelative,
                row.changedStem,
                row.changedBranch,
                row.changedElement,
                displayOptions
            ).hasContent()
        }
    )
}

private fun ResultVisibleColumns.hasChangedColumns(): Boolean {
    return changing || changedLine || changedGroup
}

private fun ResultVisibleColumns.changedHeaderSpan(): Int {
    var span = 0
    if (changing) {
        span += 1
    }
    if (changedLine || changedGroup) {
        span += 1
    }
    return span
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultScreenPreview() {
    Wuchang_liuyaoTheme {
        ResultScreen(
            result = previewResult(),
            compactSixGod = false,
            showHeavenlyStem = false,
            showAllHiddenLines = false,
            compactSixRelative = false,
            showHuiTouShengKeHint = true,
            showHuiTouChongHeHint = true,
            showDayMonthChongHeHint = true,
            markBranchXunKong = true,
            clickHighlightHint = true,
            currentSituation = "目前工作节奏较乱，团队也在调整方向",
            judgment = "宜先稳后动，等合适机会再推进",
            showSaveAction = true,
            showEditAction = false,
            editDraft = null,
            onSaveClick = {},
            onEditClick = {},
            onEditQuestionChange = {},
            onEditCurrentSituationChange = {},
            onEditJudgmentChange = {},
            onEditDismiss = {},
            onEditSave = {},
            onBackClick = {}
        )
    }
}

private fun previewResult(): DivinationResult {
    val rows = List(6) { index ->
        DivinationTableRow(
            sixGod = listOf("玄武", "白虎", "螣蛇", "勾陈", "朱雀", "青龙")[index],
            hiddenRelative = if (index == 1) "子孙" else "",
            hiddenStem = if (index == 1) "甲" else "",
            hiddenBranch = if (index == 1) "子" else "",
            hiddenElement = if (index == 1) "水" else "",
            baseRelative = listOf("父母", "兄弟", "官鬼", "妻财", "子孙", "父母")[index],
            baseStem = listOf("壬", "壬", "壬", "甲", "甲", "甲")[index],
            baseBranch = listOf("戌", "申", "午", "辰", "寅", "子")[index],
            baseElement = listOf("土", "金", "火", "土", "木", "水")[index],
            baseLineSymbol = if (index % 2 == 0) "━━━━" else "━　━",
            shiYingMark = when (index) {
                2 -> "应"
                5 -> "世"
                else -> ""
            },
            changingSymbol = if (index == 0) "o" else "",
            changedLineSymbol = if (index == 0) "━　━" else if (index % 2 == 0) "━━━━" else "━　━",
            changedRelative = if (index == 0) "妻财" else "",
            changedStem = if (index == 0) "壬" else "",
            changedBranch = if (index == 0) "戌" else "",
            changedElement = if (index == 0) "土" else ""
        )
    }
    return DivinationResult(
        question = "最近换工作是否合适",
        methodLabel = "爻名起卦",
        dateInfo = DivinationDateInfo(
            displayText = "公历 2026年5月2日 19时12分",
            timeType = DivinationTimeType.GREGORIAN,
            solarText = "公历 2026年5月2日 19时12分",
            lunarText = "农历 2026年3月16日 19时12分",
            ganzhiText = "干支 丙午年 壬辰月 辛卯日 戊戌时",
            termText = "立春",
            year = GanzhiPillar("丙", "午"),
            month = GanzhiPillar("壬", "辰"),
            day = GanzhiPillar("辛", "卯"),
            hour = GanzhiPillar("戊", "戌")
        ),
        spirits = previewSpirits(),
        dayXunKong = "午未",
        baseHexagramName = "乾为天",
        palaceName = "乾",
        hexagramType = "六冲",
        changedHexagramName = "天风姤",
        changedPalaceName = "乾",
        changedHexagramType = null,
        rowsTopDown = rows
    )
}

private fun previewSpirits(): List<DivinationSpirit> {
    return listOf(
        DivinationSpirit("禄神", "酉"),
        DivinationSpirit("羊刃", "申"),
        DivinationSpirit("文昌", "子"),
        DivinationSpirit("驿马", "巳"),
        DivinationSpirit("桃花", "子"),
        DivinationSpirit("将星", "卯"),
        DivinationSpirit("劫煞", "申"),
        DivinationSpirit("灾煞", "酉"),
        DivinationSpirit("华盖", "未"),
        DivinationSpirit("谋星", "丑"),
        DivinationSpirit("天医", "卯"),
        DivinationSpirit("天喜", "戌"),
        DivinationSpirit("贵人", "午寅")
    )
}
