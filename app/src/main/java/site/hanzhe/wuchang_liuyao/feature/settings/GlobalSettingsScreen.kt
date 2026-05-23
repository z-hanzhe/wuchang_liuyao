package site.hanzhe.wuchang_liuyao.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import site.hanzhe.wuchang_liuyao.data.settings.AppFontScale
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.feature.home.DivinationMethod
import site.hanzhe.wuchang_liuyao.ui.common.WuchangConfirmDialog
import site.hanzhe.wuchang_liuyao.ui.common.WuchangOptionPickerDialog
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

@Composable
internal fun GlobalSettingsScreen(
    showLunarInfo: Boolean,
    fontScale: Float,
    changeDayPillarAt23: Boolean,
    defaultDivinationMethod: DivinationMethod,
    defaultDivinationTimeType: DivinationTimeType,
    onShowLunarInfoChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onChangeDayPillarAt23Change: (Boolean) -> Unit,
    onDefaultDivinationMethodChange: (DivinationMethod) -> Unit,
    onDefaultDivinationTimeTypeChange: (DivinationTimeType) -> Unit,
    onBackClick: () -> Unit
) {
    var isFontScaleDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isCloseDayPillarDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isDefaultMethodDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isDefaultTimeTypeDialogVisible by rememberSaveable { mutableStateOf(false) }
    var pendingFontScale by remember(fontScale) {
        mutableStateOf(AppFontScale.normalize(fontScale))
    }
    val handleChangeDayPillarAt23 = { checked: Boolean ->
        if (changeDayPillarAt23 && !checked) {
            isCloseDayPillarDialogVisible = true
        } else {
            onChangeDayPillarAt23Change(checked)
        }
    }

    SettingsPageScaffold(
        title = "通用设置",
        onBackClick = onBackClick
    ) { innerPadding ->
        SettingsPageColumn(innerPadding = innerPadding) {
            SettingsGroup(title = "通用设置") {
                SettingsValueRow(
                    title = "调整字体大小",
                    value = AppFontScale.formatSummary(fontScale),
                    onClick = {
                        pendingFontScale = AppFontScale.normalize(fontScale)
                        isFontScaleDialogVisible = true
                    }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "显示农历卡片",
                    checked = showLunarInfo,
                    onCheckedChange = onShowLunarInfoChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "23点更替日柱",
                    checked = changeDayPillarAt23,
                    onCheckedChange = handleChangeDayPillarAt23
                )
                SettingsDivider()
                SettingsValueRow(
                    title = "默认起卦方式",
                    value = defaultDivinationMethod.label,
                    onClick = { isDefaultMethodDialogVisible = true }
                )
                SettingsDivider()
                SettingsValueRow(
                    title = "默认起卦时间",
                    value = defaultDivinationTimeType.settingLabel,
                    onClick = { isDefaultTimeTypeDialogVisible = true }
                )
            }
        }
    }

    if (isFontScaleDialogVisible) {
        FontScaleDialog(
            value = pendingFontScale,
            onValueChange = { pendingFontScale = AppFontScale.normalize(it) },
            onDismiss = { isFontScaleDialogVisible = false },
            onConfirm = {
                onFontScaleChange(pendingFontScale)
                isFontScaleDialogVisible = false
            }
        )
    }

    if (isCloseDayPillarDialogVisible) {
        CloseDayPillarConfirmDialog(
            onDismiss = { isCloseDayPillarDialogVisible = false },
            onConfirm = {
                onChangeDayPillarAt23Change(false)
                isCloseDayPillarDialogVisible = false
            }
        )
    }

    if (isDefaultMethodDialogVisible) {
        WuchangOptionPickerDialog(
            title = "默认起卦方式",
            options = DivinationMethod.entries,
            selectedOptionId = defaultDivinationMethod.name,
            onDismiss = { isDefaultMethodDialogVisible = false },
            onOptionSelected = { method ->
                onDefaultDivinationMethodChange(method)
                isDefaultMethodDialogVisible = false
            },
            optionId = { it.name },
            optionText = { it.label }
        )
    }

    if (isDefaultTimeTypeDialogVisible) {
        WuchangOptionPickerDialog(
            title = "默认起卦时间",
            options = DivinationTimeType.entries,
            selectedOptionId = defaultDivinationTimeType.name,
            onDismiss = { isDefaultTimeTypeDialogVisible = false },
            onOptionSelected = { timeType ->
                onDefaultDivinationTimeTypeChange(timeType)
                isDefaultTimeTypeDialogVisible = false
            },
            optionId = { it.name },
            optionText = { it.settingLabel }
        )
    }
}

private val DivinationTimeType.settingLabel: String
    get() = when (this) {
        DivinationTimeType.GREGORIAN -> "公历起卦"
        DivinationTimeType.LUNAR -> "农历起卦"
        DivinationTimeType.GANZHI -> "干支起卦"
    }

@Composable
private fun FontScaleDialog(
    value: Float,
    onValueChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "调整字体大小",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Wuchang_liuyaoTheme(appFontScale = value) {
                    FontScalePreviewCard()
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = AppFontScale.formatSummary(value),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    FontScaleSlider(
                        value = value,
                        onValueChange = onValueChange
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "x0.70倍",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Box(modifier = Modifier.weight(1f))
                        Text(
                            text = "x1.30倍",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DialogActionButton(
                        modifier = Modifier.weight(1f),
                        text = "取消",
                        isPrimary = false,
                        onClick = onDismiss
                    )
                    DialogActionButton(
                        modifier = Modifier.weight(1f),
                        text = "应用",
                        isPrimary = true,
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}

@Composable
private fun CloseDayPillarConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    WuchangConfirmDialog(
        title = "关闭确认",
        message = "23点更替日柱是主流规则，您确定要关闭吗？",
        confirmText = "确定关闭",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun FontScalePreviewCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "预览效果",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "无常六爻排盘",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontScaleSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        activeTickColor = MaterialTheme.colorScheme.onPrimary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        inactiveTickColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
    )

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = AppFontScale.Min..AppFontScale.Max,
        steps = AppFontScale.DiscreteSteps,
        colors = sliderColors,
        thumb = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 6.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(8.dp),
                colors = sliderColors,
                drawStopIndicator = null
            )
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GlobalSettingsScreenPreview() {
    Wuchang_liuyaoTheme {
        GlobalSettingsScreen(
            showLunarInfo = false,
            fontScale = AppFontScale.Default,
            changeDayPillarAt23 = true,
            defaultDivinationMethod = DivinationMethod.YAO_NAME,
            defaultDivinationTimeType = DivinationTimeType.GREGORIAN,
            onShowLunarInfoChange = {},
            onFontScaleChange = {},
            onChangeDayPillarAt23Change = {},
            onDefaultDivinationMethodChange = {},
            onDefaultDivinationTimeTypeChange = {},
            onBackClick = {}
        )
    }
}
