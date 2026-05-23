package site.hanzhe.wuchang_liuyao.feature.time

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.domain.time.LunarDate
import site.hanzhe.wuchang_liuyao.domain.time.LunarMonthOption
import site.hanzhe.wuchang_liuyao.domain.time.SolarDateTime
import site.hanzhe.wuchang_liuyao.domain.time.formatSelectableLunarMonth
import site.hanzhe.wuchang_liuyao.ui.common.noRippleClick
import site.hanzhe.wuchang_liuyao.ui.common.TopBarTextAction
import site.hanzhe.wuchang_liuyao.ui.common.WuchangOptionPickerDialog
import site.hanzhe.wuchang_liuyao.ui.common.WuchangTopAppBar
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

@Composable
internal fun SelectTimeScreen(
    uiState: TimeSelectionUiState,
    onTypeSelected: (DivinationTimeType) -> Unit,
    onPickerClick: (TimePickerType) -> Unit,
    onPickerOptionSelected: (String) -> Unit,
    onGanzhiFieldClick: (GanzhiFieldType) -> Unit,
    onGanzhiFieldClear: (GanzhiFieldType) -> Unit,
    onGanzhiDeleteDismiss: () -> Unit,
    onGanzhiOptionSelected: (String) -> Unit,
    onGanzhiReset: () -> Unit,
    onPickerDismiss: () -> Unit,
    onUseCurrentTimeClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            SelectTimeTopAppBar(
                onUseCurrentTimeClick = onUseCurrentTimeClick,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .noRippleClick(onGanzhiDeleteDismiss)
                .padding(
                    start = 12.dp,
                    top = innerPadding.calculateTopPadding() + 14.dp,
                    end = 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + 14.dp
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TimeTypeSegmentedRow(
                selectedType = uiState.selectedType,
                onTypeSelected = onTypeSelected
            )

            when (uiState.selectedType) {
                DivinationTimeType.GREGORIAN -> TimeSelectionFormCard {
                    TimeSelectionFieldRow(
                        label = "年份",
                        value = uiState.solarDateTime.year.toString(),
                        onClick = { onPickerClick(TimePickerType.SolarYear) }
                    )
                    TimeSelectionFieldRow(
                        label = "月份",
                        value = uiState.solarDateTime.month.toString(),
                        onClick = { onPickerClick(TimePickerType.SolarMonth) }
                    )
                    TimeSelectionFieldRow(
                        label = "日期",
                        value = uiState.solarDateTime.day.toString(),
                        onClick = { onPickerClick(TimePickerType.SolarDay) }
                    )
                    TimeSelectionFieldRow(
                        label = "小时",
                        value = uiState.solarDateTime.hour.toString(),
                        onClick = { onPickerClick(TimePickerType.Hour) }
                    )
                    TimeSelectionFieldRow(
                        label = "分钟",
                        value = uiState.solarDateTime.minute.toString(),
                        onClick = { onPickerClick(TimePickerType.Minute) }
                    )
                }

                DivinationTimeType.LUNAR -> TimeSelectionFormCard {
                    TimeSelectionFieldRow(
                        label = "年份",
                        value = uiState.lunarDate.year.toString(),
                        onClick = { onPickerClick(TimePickerType.LunarYear) }
                    )
                    TimeSelectionFieldRow(
                        label = "月份",
                        value = formatSelectableLunarMonth(
                            month = uiState.lunarDate.month,
                            isLeapMonth = uiState.lunarDate.isLeapMonth
                        ),
                        onClick = { onPickerClick(TimePickerType.LunarMonth) }
                    )
                    TimeSelectionFieldRow(
                        label = "日期",
                        value = uiState.lunarDate.day.toString(),
                        onClick = { onPickerClick(TimePickerType.LunarDay) }
                    )
                    TimeSelectionFieldRow(
                        label = "小时",
                        value = uiState.solarDateTime.hour.toString(),
                        onClick = { onPickerClick(TimePickerType.Hour) }
                    )
                    TimeSelectionFieldRow(
                        label = "分钟",
                        value = uiState.solarDateTime.minute.toString(),
                        onClick = { onPickerClick(TimePickerType.Minute) }
                    )
                }

                DivinationTimeType.GANZHI -> GanzhiContentCard(
                    selectionState = uiState.ganzhiSelectionState,
                    onFieldClick = onGanzhiFieldClick,
                    onFieldClear = onGanzhiFieldClear,
                    onDeleteDismiss = onGanzhiDeleteDismiss,
                    onResetClick = onGanzhiReset,
                    onOptionClick = onGanzhiOptionSelected
                )
            }

            Button(
                onClick = onConfirmClick,
                enabled = uiState.selectedType != DivinationTimeType.GANZHI ||
                    uiState.ganzhiSelectionState.isRequiredFieldsComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "确认",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    buildTimePickerDialogState(uiState)?.let { dialogState ->
        WuchangOptionPickerDialog(
            title = dialogState.title,
            options = dialogState.options,
            selectedOptionId = dialogState.selectedOptionId,
            onDismiss = onPickerDismiss,
            onOptionSelected = { option -> onPickerOptionSelected(option.id) },
            optionId = { it.id },
            optionText = { it.label }
        )
    }
}

@Composable
private fun SelectTimeTopAppBar(
    onUseCurrentTimeClick: () -> Unit,
    onBackClick: () -> Unit
) {
    WuchangTopAppBar(
        title = "选择时间",
        onBackClick = onBackClick,
        actions = {
            TopBarTextAction(
                text = "现在",
                onClick = onUseCurrentTimeClick
            )
        }
    )
}

@Composable
private fun TimeTypeSegmentedRow(
    selectedType: DivinationTimeType,
    onTypeSelected: (DivinationTimeType) -> Unit
) {
    val segmentTypes = DivinationTimeType.entries
    val indicatorSpacing = 4.dp
    val segmentHeight = 34.dp
    val selectedIndex = segmentTypes.indexOf(selectedType).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(4.dp)
    ) {
        val segmentWidth = (maxWidth - indicatorSpacing * (segmentTypes.size - 1)) / segmentTypes.size
        val indicatorOffset by animateDpAsState(
            targetValue = (segmentWidth + indicatorSpacing) * selectedIndex,
            animationSpec = tween(durationMillis = 240),
            label = "timeTypeIndicatorOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .height(segmentHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(segmentHeight),
            horizontalArrangement = Arrangement.spacedBy(indicatorSpacing)
        ) {
            segmentTypes.forEach { type ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .noRippleClick(onClick = { onTypeSelected(type) })
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.label,
                        color = if (selectedType == type) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 14.sp,
                        fontWeight = if (selectedType == type) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSelectionFormCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun TimeSelectionFieldRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.35f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(44.dp * fontScale),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(10.dp)
                )
                .noRippleClick(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "›",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun GanzhiContentCard(
    selectionState: GanzhiSelectionState,
    onFieldClick: (GanzhiFieldType) -> Unit,
    onFieldClear: (GanzhiFieldType) -> Unit,
    onDeleteDismiss: () -> Unit,
    onResetClick: () -> Unit,
    onOptionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClick(onDeleteDismiss)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "月份地支和日辰干支是必填项",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "*",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "[重置]",
                modifier = Modifier.noRippleClick(onResetClick),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        GanzhiInputRow(
            selectionState = selectionState,
            onFieldClick = onFieldClick,
            onFieldClear = onFieldClear
        )
        GanzhiOptionPanel(
            selectionState = selectionState,
            onOptionClick = onOptionClick
        )
    }
}

@Composable
private fun GanzhiInputRow(
    selectionState: GanzhiSelectionState,
    onFieldClick: (GanzhiFieldType) -> Unit,
    onFieldClear: (GanzhiFieldType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GanzhiFieldGroup(
            stem = selectionState.yearStem,
            branch = selectionState.yearBranch,
            stemField = GanzhiFieldType.YearStem,
            branchField = GanzhiFieldType.YearBranch,
            activeField = selectionState.activeField,
            deleteVisibleField = selectionState.deleteVisibleField,
            suffix = "年",
            onFieldClick = onFieldClick,
            onFieldClear = onFieldClear
        )
        GanzhiFieldGroup(
            stem = selectionState.monthStem,
            branch = selectionState.monthBranch,
            stemField = GanzhiFieldType.MonthStem,
            branchField = GanzhiFieldType.MonthBranch,
            activeField = selectionState.activeField,
            deleteVisibleField = selectionState.deleteVisibleField,
            suffix = "月",
            onFieldClick = onFieldClick,
            onFieldClear = onFieldClear
        )
        GanzhiFieldGroup(
            stem = selectionState.dayStem,
            branch = selectionState.dayBranch,
            stemField = GanzhiFieldType.DayStem,
            branchField = GanzhiFieldType.DayBranch,
            activeField = selectionState.activeField,
            deleteVisibleField = selectionState.deleteVisibleField,
            suffix = "日",
            onFieldClick = onFieldClick,
            onFieldClear = onFieldClear
        )
        GanzhiFieldGroup(
            stem = selectionState.hourStem,
            branch = selectionState.hourBranch,
            stemField = GanzhiFieldType.HourStem,
            branchField = GanzhiFieldType.HourBranch,
            activeField = selectionState.activeField,
            deleteVisibleField = selectionState.deleteVisibleField,
            suffix = "时",
            onFieldClick = onFieldClick,
            onFieldClear = onFieldClear
        )
    }
}

@Composable
private fun RowScope.GanzhiFieldGroup(
    stem: String?,
    branch: String?,
    stemField: GanzhiFieldType,
    branchField: GanzhiFieldType,
    activeField: GanzhiFieldType,
    deleteVisibleField: GanzhiFieldType?,
    suffix: String,
    onFieldClick: (GanzhiFieldType) -> Unit,
    onFieldClear: (GanzhiFieldType) -> Unit
) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        GanzhiInputUnderlineField(
            value = stem,
            isActive = activeField == stemField,
            showDelete = deleteVisibleField == stemField,
            onClick = { onFieldClick(stemField) },
            onClearClick = { onFieldClear(stemField) }
        )
        GanzhiInputUnderlineField(
            value = branch,
            isActive = activeField == branchField,
            showDelete = deleteVisibleField == branchField,
            onClick = { onFieldClick(branchField) },
            onClearClick = { onFieldClear(branchField) }
        )
        Text(
            text = suffix,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GanzhiInputUnderlineField(
    value: String?,
    isActive: Boolean,
    showDelete: Boolean,
    onClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.35f)
    Box(
        modifier = Modifier
            .width(22.dp * fontScale)
            .height(26.dp * fontScale)
            .noRippleClick(onClick),
        contentAlignment = Alignment.Center
    ) {
        if (showDelete) {
            Text(
                text = "×",
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else if (value.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .width(14.dp * fontScale)
                    .height(2.dp)
                    .background(
                        if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
                        }
                    )
            )
        } else {
            Text(
                text = value,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (showDelete) {
            Text(
                text = "×",
                modifier = Modifier.noRippleClick(onClearClick),
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GanzhiOptionPanel(
    selectionState: GanzhiSelectionState,
    onOptionClick: (String) -> Unit
) {
    val options = selectionState.optionsFor(selectionState.activeField)
    val selectedValue = selectionState.valueOf(selectionState.activeField)
    val rowSize = if (selectionState.activeField.isStemField()) 5 else 6

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                text = selectionState.activeField.panelTitle(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        options.chunked(rowSize).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    GanzhiOptionChip(
                        modifier = Modifier.weight(1f),
                        value = option,
                        isSelected = option == selectedValue,
                        onClick = { onOptionClick(option) }
                    )
                }
                repeat(rowSize - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun GanzhiFieldType.isStemField(): Boolean {
    return when (this) {
        GanzhiFieldType.YearStem,
        GanzhiFieldType.MonthStem,
        GanzhiFieldType.DayStem,
        GanzhiFieldType.HourStem -> true

        GanzhiFieldType.YearBranch,
        GanzhiFieldType.MonthBranch,
        GanzhiFieldType.DayBranch,
        GanzhiFieldType.HourBranch -> false
    }
}

private fun GanzhiFieldType.panelTitle(): String {
    return when (this) {
        GanzhiFieldType.YearStem -> "选择年份天干"
        GanzhiFieldType.YearBranch -> "选择年份地支"
        GanzhiFieldType.MonthStem -> "选择月份天干"
        GanzhiFieldType.MonthBranch -> "选择月份地支"
        GanzhiFieldType.DayStem -> "选择日期天干"
        GanzhiFieldType.DayBranch -> "选择日期地支"
        GanzhiFieldType.HourStem -> "选择时辰天干"
        GanzhiFieldType.HourBranch -> "选择时辰地支"
    }
}

@Composable
private fun GanzhiOptionChip(
    modifier: Modifier = Modifier,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.2f)
    Box(
        modifier = modifier
            .height(42.dp * fontScale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .noRippleClick(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SelectTimeScreenPreview() {
    Wuchang_liuyaoTheme {
        SelectTimeScreen(
            uiState = TimeSelectionUiState(
                selectedType = DivinationTimeType.GREGORIAN,
                solarDateTime = SolarDateTime(
                    year = 2026,
                    month = 3,
                    day = 28,
                    hour = 10,
                    minute = 0
                ),
                lunarDate = LunarDate(
                    year = 2026,
                    month = 2,
                    day = 10,
                    isLeapMonth = false
                ),
                ganzhiText = "丙午年    辛卯月    庚子日",
                ganzhiSelectionState = GanzhiSelectionState(),
                solarYearOptions = (2020..2028).toList(),
                solarMonthOptions = (1..12).toList(),
                solarDayOptions = (1..31).toList(),
                lunarYearOptions = (2020..2028).toList(),
                lunarMonthOptions = listOf(
                    LunarMonthOption(month = 1, isLeapMonth = false),
                    LunarMonthOption(month = 2, isLeapMonth = false),
                    LunarMonthOption(month = 2, isLeapMonth = true)
                ),
                lunarDayOptions = (1..30).toList()
            ),
            onTypeSelected = {},
            onPickerClick = {},
            onPickerOptionSelected = {},
            onGanzhiFieldClick = {},
            onGanzhiFieldClear = {},
            onGanzhiDeleteDismiss = {},
            onGanzhiOptionSelected = {},
            onGanzhiReset = {},
            onPickerDismiss = {},
            onUseCurrentTimeClick = {},
            onConfirmClick = {},
            onBackClick = {}
        )
    }
}
