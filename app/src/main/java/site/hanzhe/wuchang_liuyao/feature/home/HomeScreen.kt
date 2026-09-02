package site.hanzhe.wuchang_liuyao.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.hanzhe.wuchang_liuyao.R
import site.hanzhe.wuchang_liuyao.domain.time.CalendarSummary
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTime
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.domain.time.SolarDateTime
import site.hanzhe.wuchang_liuyao.ui.common.noRippleClick
import site.hanzhe.wuchang_liuyao.ui.common.WuchangOptionPickerDialog
import site.hanzhe.wuchang_liuyao.ui.common.WuchangOptionSelectorSheet
import site.hanzhe.wuchang_liuyao.ui.common.ChevronIcon
import site.hanzhe.wuchang_liuyao.ui.common.TopBarMenuIcon
import site.hanzhe.wuchang_liuyao.ui.common.WuchangTopAppBar
import site.hanzhe.wuchang_liuyao.ui.theme.AppPrimaryDark
import site.hanzhe.wuchang_liuyao.ui.theme.AppTextHint
import site.hanzhe.wuchang_liuyao.ui.theme.LocalAppFontScale
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme
import kotlinx.coroutines.delay

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    showLunarInfo: Boolean,
    calendarSummary: CalendarSummary?,
    divinationTime: DivinationTime,
    onQuestionChange: (String) -> Unit,
    onTimeClick: () -> Unit,
    onCalendarSummaryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStartDivinationClick: () -> Unit,
    onMethodClick: () -> Unit,
    onMethodDismiss: () -> Unit,
    onMethodSelected: (DivinationMethod) -> Unit,
    onHexagramTrigramSelected: (HexagramTrigramField, TrigramOption) -> Unit,
    onResetCurrentMethodValues: () -> Unit,
    onYaoClick: (Int) -> Unit,
    onPointLineClick: (Int) -> Unit,
    onPointLineMovingChange: (Int, Boolean) -> Unit,
    onOnlineShakeClick: () -> Unit,
    onYaoValueDismiss: () -> Unit,
    onYaoValueSelected: (String) -> Unit
) {
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isStartDivinationEnabled = uiState.canStartDivination()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { HomeTopAppBar(onSettingsClick = onSettingsClick) },
        bottomBar = {
            BottomActionBar(
                modifier = Modifier.padding(bottom = bottomPadding),
                enabled = isStartDivinationEnabled,
                onClick = onStartDivinationClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                end = 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                if (showLunarInfo) {
                    calendarSummary?.let {
                        CalendarSummaryCard(
                            calendarSummary = it,
                            onClick = onCalendarSummaryClick
                        )
                    }
                }
            }
            item {
                QuestionRow(
                    question = uiState.question,
                    onQuestionChange = onQuestionChange
                )
            }
            item {
                SelectableDetailRow(
                    label = divinationTime.type.homeTimeLabel,
                    value = divinationTime.displayValueWithoutType(),
                    onClick = onTimeClick
                )
            }
            item {
                SelectableDetailRow(
                    label = "起卦方式：",
                    value = uiState.selectedMethod.label,
                    onClick = onMethodClick
                )
            }
            item {
                when (uiState.selectedMethod.inputSectionType) {
                    InputSectionType.YAO_NAME -> {
                        YaoSection(
                            title = uiState.selectedMethod.label,
                            values = uiState.selectedYaoValues,
                            onReset = onResetCurrentMethodValues,
                            onYaoClick = onYaoClick
                        )
                    }

                    InputSectionType.HEXAGRAM_NAME -> {
                        HexagramNameSection(
                            title = uiState.selectedMethod.label,
                            selection = uiState.hexagramNameSelection,
                            onReset = onResetCurrentMethodValues,
                            onTrigramSelected = onHexagramTrigramSelected
                        )
                    }

                    InputSectionType.COIN -> {
                        YaoSection(
                            title = uiState.selectedMethod.label,
                            values = uiState.selectedCoinValues,
                            onReset = onResetCurrentMethodValues,
                            onYaoClick = onYaoClick
                        )
                    }

                    InputSectionType.POINT_SELECT -> {
                        PointSelectionSection(
                            title = uiState.selectedMethod.label,
                            lines = uiState.pointSelectionLines,
                            onReset = onResetCurrentMethodValues,
                            onLineClick = onPointLineClick,
                            onMovingCheckedChange = onPointLineMovingChange
                        )
                    }

                    InputSectionType.ONLINE_SHAKE -> {
                        OnlineShakeSection(
                            title = uiState.selectedMethod.label,
                            generatedValues = uiState.onlineShakeValues,
                            coinFaces = uiState.onlineShakeCoinFaces,
                            nextIndex = uiState.nextOnlineShakeIndex,
                            isAnimating = uiState.isOnlineShakeAnimating,
                            onReset = onResetCurrentMethodValues,
                            onShakeClick = onOnlineShakeClick
                        )
                    }

                    null -> {
                        MethodPlaceholderSection(
                            title = uiState.selectedMethod.label,
                            message = if (uiState.selectedMethod == DivinationMethod.AUTO) {
                                "电脑起卦可直接点击排盘按钮"
                            } else {
                                "${uiState.selectedMethod.label}功能正在开发中"
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.isMethodSheetVisible) {
        MethodSelectorSheet(
            selectedMethod = uiState.selectedMethod,
            onDismiss = onMethodDismiss,
            onMethodSelected = onMethodSelected
        )
    }

    uiState.activeYaoPicker?.let { pickerState ->
        val currentValues = when (pickerState.sectionType) {
            InputSectionType.YAO_NAME -> uiState.selectedYaoValues
            InputSectionType.HEXAGRAM_NAME -> emptyList()
            InputSectionType.COIN -> uiState.selectedCoinValues
            InputSectionType.POINT_SELECT -> emptyList()
            InputSectionType.ONLINE_SHAKE -> emptyList()
        }
        val currentOptions = when (pickerState.sectionType) {
            InputSectionType.YAO_NAME -> YaoValueOptions
            InputSectionType.HEXAGRAM_NAME -> emptyList()
            InputSectionType.COIN -> CoinValueOptions
            InputSectionType.POINT_SELECT -> emptyList()
            InputSectionType.ONLINE_SHAKE -> emptyList()
        }

        if (currentValues.isNotEmpty() && currentOptions.isNotEmpty()) {
            YaoValueSelectorSheet(
                selectedValue = currentValues[pickerState.index],
                options = currentOptions,
                onDismiss = onYaoValueDismiss,
                onValueSelected = onYaoValueSelected
            )
        }
    }
}

@Composable
private fun HomeTopAppBar(onSettingsClick: () -> Unit) {
    WuchangTopAppBar(
        title = "无常六爻排盘",
        actions = {
            TopBarMenuIcon(onClick = onSettingsClick)
        }
    )
}

@Composable
private fun CalendarSummaryCard(
    calendarSummary: CalendarSummary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(14.dp)
            )
            .noRippleClick(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MaterialTheme.colorScheme.secondary, AppPrimaryDark)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryCell(
                    modifier = Modifier.weight(1f),
                    text = "农历",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                SummaryCell(
                    modifier = Modifier.weight(2f),
                    text = calendarSummary.lunarYearText
                )
                SummaryCell(
                    modifier = Modifier.weight(1f),
                    text = calendarSummary.lunarMonthText
                )
                SummaryCell(
                    modifier = Modifier.weight(1f),
                    text = calendarSummary.lunarDayText
                )
                SummaryCell(
                    modifier = Modifier.weight(1f),
                    text = calendarSummary.termText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = calendarSummary.ganzhiSummaryText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SummaryCell(
    modifier: Modifier,
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}

@Composable
private fun QuestionRow(
    question: String,
    onQuestionChange: (String) -> Unit
) {
    FormRow(label = "输入问念：") {
        BasicTextField(
            value = question,
            onValueChange = onQuestionChange,
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                textAlign = TextAlign.End
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (question.isEmpty()) {
                        Text(
                            text = "请输入您想要咨询的问题",
                            color = AppTextHint,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun SelectableDetailRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    FormRow(label = label) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClick(onClick),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArrowValueText(value = value, showArrow = true)
        }
    }
}

@Composable
private fun ArrowValueText(
    value: String,
    showArrow: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            textAlign = TextAlign.End
        )
        if (showArrow) {
            Spacer(modifier = Modifier.width(4.dp))
            ChevronIcon(iconSize = 16.dp, tint = AppTextHint)
        }
    }
}

@Composable
private fun FormRow(
    label: String,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            modifier = Modifier.padding(end = 12.dp)
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            trailingContent()
        }
    }
}

@Composable
private fun MethodPlaceholderSection(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionTitle(
            title = title,
            showReset = false,
            onReset = {}
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun YaoSection(
    title: String,
    values: List<String>,
    onReset: () -> Unit,
    onYaoClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionTitle(
            title = title,
            showReset = true,
            onReset = onReset
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            YaoNames.forEachIndexed { index, yaoName ->
                YaoInputRow(
                    label = yaoName,
                    value = values[index],
                    onClick = { onYaoClick(index) }
                )
            }
        }
    }
}

/** 展示围绕中轴对称排列的本卦和变卦选择框。 */
@Composable
private fun HexagramNameSection(
    title: String,
    selection: HexagramNameSelectionState,
    onReset: () -> Unit,
    onTrigramSelected: (HexagramTrigramField, TrigramOption) -> Unit
) {
    var activeField by remember { mutableStateOf<HexagramTrigramField?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle(
            title = title,
            showReset = true,
            onReset = onReset
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HexagramColumnHeaders()
            HexagramTrigramInputRow(
                label = "上卦",
                baseSelection = selection.baseUpper,
                changedSelection = selection.changedUpper,
                onBaseClick = { activeField = HexagramTrigramField.BASE_UPPER },
                onChangedClick = { activeField = HexagramTrigramField.CHANGED_UPPER }
            )
            HexagramTrigramInputRow(
                label = "下卦",
                baseSelection = selection.baseLower,
                changedSelection = selection.changedLower,
                onBaseClick = { activeField = HexagramTrigramField.BASE_LOWER },
                onChangedClick = { activeField = HexagramTrigramField.CHANGED_LOWER }
            )
        }
    }

    activeField?.let { field ->
        WuchangOptionPickerDialog(
            title = field.pickerTitle,
            options = TrigramOption.entries,
            selectedOptionId = selection.selectionOf(field)?.name.orEmpty(),
            onDismiss = { activeField = null },
            onOptionSelected = { trigram ->
                onTrigramSelected(field, trigram)
                activeField = null
            },
            optionId = { trigram -> trigram.name },
            optionText = { trigram -> trigram.displayText }
        )
    }
}

/** 展示本卦与变卦两列标题。 */
@Composable
private fun HexagramColumnHeaders() {
    Row(
        modifier = Modifier
            .widthIn(max = 292.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(42.dp))
        Text(
            text = "本卦",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "变卦",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(42.dp))
    }
}

/** 按草图布局展示一行本卦与变卦选择框。 */
@Composable
private fun HexagramTrigramInputRow(
    label: String,
    baseSelection: TrigramOption?,
    changedSelection: TrigramOption?,
    onBaseClick: () -> Unit,
    onChangedClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .widthIn(max = 292.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HexagramFieldLabel(
            text = label,
            modifier = Modifier.width(42.dp)
        )
        TrigramPickerField(
            modifier = Modifier.weight(1f),
            selection = baseSelection,
            emptyText = "请选择",
            onClick = onBaseClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        TrigramPickerField(
            modifier = Modifier.weight(1f),
            selection = changedSelection,
            emptyText = "请选择",
            onClick = onChangedClick
        )
        HexagramFieldLabel(
            text = label,
            modifier = Modifier.width(42.dp)
        )
    }
}

/** 展示选择框外侧的上卦或下卦标签。 */
@Composable
private fun HexagramFieldLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

/** 展示与首页风格一致的八卦选择框。 */
@Composable
private fun TrigramPickerField(
    modifier: Modifier,
    selection: TrigramOption?,
    emptyText: String,
    onClick: () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.35f)

    Box(
        modifier = modifier
            .height(38.dp * fontScale)
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
                ),
                shape = RoundedCornerShape(9.dp)
            )
            .noRippleClick(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selection?.displayText ?: emptyText,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = if (selection == null) {
                AppTextHint
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 获取八卦选择弹窗标题。 */
private val HexagramTrigramField.pickerTitle: String
    get() = when (this) {
        HexagramTrigramField.BASE_UPPER -> "选择本卦上卦"
        HexagramTrigramField.BASE_LOWER -> "选择本卦下卦"
        HexagramTrigramField.CHANGED_UPPER -> "选择变卦上卦"
        HexagramTrigramField.CHANGED_LOWER -> "选择变卦下卦"
    }

@Composable
private fun PointSelectionSection(
    title: String,
    lines: List<PointSelectionLineState>,
    onReset: () -> Unit,
    onLineClick: (Int) -> Unit,
    onMovingCheckedChange: (Int, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SectionTitle(
            title = title,
            showReset = true,
            onReset = onReset
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PointSelectionYaoNames.forEachIndexed { index, yaoName ->
                PointSelectionInputRow(
                    label = yaoName,
                    lineState = lines[index],
                    onLineClick = { onLineClick(index) },
                    onMovingCheckedChange = { checked ->
                        onMovingCheckedChange(index, checked)
                    }
                )
            }
        }
    }
}

@Composable
private fun OnlineShakeSection(
    title: String,
    generatedValues: List<String>,
    coinFaces: List<OnlineShakeCoinFace>,
    nextIndex: Int,
    isAnimating: Boolean,
    onReset: () -> Unit,
    onShakeClick: () -> Unit
) {
    var displayCoinFaces by remember(coinFaces, isAnimating) { mutableStateOf(coinFaces) }
    val horizontalGap = 18.dp

    LaunchedEffect(coinFaces, isAnimating) {
        if (!isAnimating) {
            displayCoinFaces = coinFaces
            return@LaunchedEffect
        }
        var isFront = false
        while (true) {
            displayCoinFaces = List(coinFaces.size) {
                if (isFront) {
                    OnlineShakeCoinFace.FRONT
                } else {
                    OnlineShakeCoinFace.BACK
                }
            }
            isFront = !isFront
            delay(180)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(
            title = title,
            showReset = true,
            onReset = onReset
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalGap),
            horizontalArrangement = Arrangement.spacedBy(horizontalGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OnlineShakeCoinsPanel(
                coinFaces = displayCoinFaces,
                isAnimating = isAnimating,
                isCompleted = nextIndex < 0,
                onClick = onShakeClick
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                YaoNames.forEachIndexed { index, yaoName ->
                    OnlineShakeResultRow(
                        label = yaoName,
                        value = generatedValues[index]
                    )
                }
            }
        }
    }
}

@Composable
private fun OnlineShakeCoinsPanel(
    coinFaces: List<OnlineShakeCoinFace>,
    isAnimating: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .height(272.dp)
            .noRippleClick {
                if (!isCompleted || isAnimating) {
                    onClick()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        coinFaces.forEach { coinFace ->
            OnlineShakeCoinPlaceholder(face = coinFace)
        }
    }
}

@Composable
private fun OnlineShakeCoinPlaceholder(face: OnlineShakeCoinFace) {
    val imageRes = when (face) {
        OnlineShakeCoinFace.FRONT -> R.drawable.ic_coin_front
        OnlineShakeCoinFace.BACK -> R.drawable.ic_coin_back
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = if (face == OnlineShakeCoinFace.FRONT) {
            "铜钱正面"
        } else {
            "铜钱背面"
        },
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(82.dp)
    )
}

@Composable
private fun OnlineShakeResultRow(
    label: String,
    value: String
) {
    val option = findYaoValueOption(value)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(56.dp)
        )
        Box(
            modifier = Modifier.height(26.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (option != null) {
                YaoValueOptionContent(
                    option = option,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Spacer(modifier = Modifier.width(90.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    showReset: Boolean,
    onReset: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(17.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        if (showReset) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "重置",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.noRippleClick(onReset)
            )
        }
    }
}

@Composable
private fun PointSelectionInputRow(
    label: String,
    lineState: PointSelectionLineState,
    onLineClick: () -> Unit,
    onMovingCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(end = 7.dp)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 7.dp)
                .height(26.dp)
                .noRippleClick(onLineClick),
            contentAlignment = Alignment.Center
        ) {
            YaoLineSymbol(
                lineType = if (lineState.isYang) {
                    YaoLineType.YANG
                } else {
                    YaoLineType.YIN
                },
                color = MaterialTheme.colorScheme.onSurface,
                lengthScale = 1.6f
            )
        }
        Box(
            modifier = Modifier.padding(start = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Checkbox(
                    modifier = Modifier.size(20.dp),
                    checked = lineState.isMoving,
                    onCheckedChange = onMovingCheckedChange
                )
            }
        }
    }
}

@Composable
private fun YaoInputRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.35f)
    val yaoValueOption = findYaoValueOption(value)
    val isUnselected = value == UnselectedYaoValue

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            modifier = Modifier.width(56.dp)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(38.dp * fontScale)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)),
                    shape = RoundedCornerShape(9.dp)
                )
                .noRippleClick(onClick)
                .padding(start = 18.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (yaoValueOption == null) {
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    color = if (isUnselected) {
                        AppTextHint
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start
                )
            } else {
                YaoValueOptionContent(
                    option = yaoValueOption,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            ChevronIcon(iconSize = 16.dp, tint = AppTextHint)
        }
    }
}

@Composable
private fun MethodSelectorSheet(
    selectedMethod: DivinationMethod,
    onDismiss: () -> Unit,
    onMethodSelected: (DivinationMethod) -> Unit
) {
    WuchangOptionSelectorSheet(
        options = DivinationMethod.entries,
        selectedOption = selectedMethod,
        onDismiss = onDismiss,
        onOptionSelected = onMethodSelected,
        optionText = { it.label }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun YaoValueSelectorSheet(
    selectedValue: String,
    options: List<Any>,
    onDismiss: () -> Unit,
    onValueSelected: (String) -> Unit
) {
    val appFontScale = LocalAppFontScale.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Wuchang_liuyaoTheme(appFontScale = appFontScale) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    options.forEachIndexed { index, option ->
                        val yaoValueOption = option as? YaoValueOption
                        val optionValue = yaoValueOption?.label ?: option.toString()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onValueSelected(optionValue) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (yaoValueOption == null) {
                                Text(
                                    text = optionValue,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = if (optionValue == selectedValue) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Medium
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                YaoValueOptionContent(
                                    option = yaoValueOption,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (optionValue == selectedValue) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Medium
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text(
                                text = if (optionValue == selectedValue) "当前" else "选择",
                                color = if (optionValue == selectedValue) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    AppTextHint
                                },
                                fontSize = 12.sp
                            )
                        }
                        if (index != options.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Button(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "点击排盘",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun YaoValueOptionContent(
    option: YaoValueOption,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Medium
) {
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.35f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = option.label,
            color = color,
            fontSize = 14.sp,
            fontWeight = fontWeight
        )
        Spacer(modifier = Modifier.width(12.dp * fontScale))
        YaoLineSymbol(
            lineType = option.lineType,
            color = color
        )
        option.marker?.let { marker ->
            Spacer(modifier = Modifier.width(10.dp * fontScale))
            Text(
                text = marker,
                color = color,
                fontSize = 14.sp,
                fontWeight = fontWeight
            )
        }
    }
}

@Composable
private fun YaoLineSymbol(
    lineType: YaoLineType,
    color: Color,
    lengthScale: Float = 1f
) {
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.35f)
    val fullWidth = 48.dp * fontScale * lengthScale
    val lineHeight = 8.dp * fontScale
    val gapWidth = 10.dp * fontScale
    val segmentWidth = (fullWidth - gapWidth) / 2f

    if (lineType == YaoLineType.YANG) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(fullWidth)
                    .height(lineHeight)
                    .background(color)
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(gapWidth)
        ) {
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .height(lineHeight)
                    .background(color)
            )
            Box(
                modifier = Modifier
                    .width(segmentWidth)
                    .height(lineHeight)
                    .background(color)
            )
        }
    }
}

private val DivinationTimeType.homeTimeLabel: String
    get() = when (this) {
        DivinationTimeType.GREGORIAN -> "公历时间："
        DivinationTimeType.LUNAR -> "农历时间："
        DivinationTimeType.GANZHI -> "干支时间："
    }

private fun DivinationTime.displayValueWithoutType(): String {
    return value.removePrefix("${type.label} ")
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    Wuchang_liuyaoTheme {
        HomeScreen(
            uiState = HomeUiState(selectedYaoValues = debugYaoSelectedValues()),
            showLunarInfo = true,
            calendarSummary = CalendarSummary(
                lunarYearText = "二〇二六年",
                lunarMonthText = "二月",
                lunarDayText = "初八",
                termText = "",
                ganzhiSummaryText = "丙午年    辛卯月    庚子日"
            ),
            divinationTime = DivinationTime(
                type = DivinationTimeType.GREGORIAN,
                solarDateTime = SolarDateTime(
                    year = 2026,
                    month = 3,
                    day = 28,
                    hour = 10,
                    minute = 0
                ),
                value = "公历 2026年3月28日 10时0分"
            ),
            onQuestionChange = {},
            onTimeClick = {},
            onCalendarSummaryClick = {},
            onSettingsClick = {},
            onStartDivinationClick = {},
            onMethodClick = {},
            onMethodDismiss = {},
            onMethodSelected = {},
            onHexagramTrigramSelected = { _, _ -> },
            onResetCurrentMethodValues = {},
            onYaoClick = {},
            onPointLineClick = {},
            onPointLineMovingChange = { _, _ -> },
            onOnlineShakeClick = {},
            onYaoValueDismiss = {},
            onYaoValueSelected = {}
        )
    }
}
