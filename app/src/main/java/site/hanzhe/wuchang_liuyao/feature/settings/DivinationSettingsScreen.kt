package site.hanzhe.wuchang_liuyao.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

@Composable
internal fun DivinationSettingsScreen(
    compactSixGod: Boolean,
    showHeavenlyStem: Boolean,
    showAllHiddenLines: Boolean,
    compactSixRelative: Boolean,
    showHuiTouShengKeHint: Boolean,
    showHuiTouChongHeHint: Boolean,
    showDayMonthChongHeHint: Boolean,
    markBranchXunKong: Boolean,
    clickHighlightHint: Boolean,
    onCompactSixGodChange: (Boolean) -> Unit,
    onShowHeavenlyStemChange: (Boolean) -> Unit,
    onShowAllHiddenLinesChange: (Boolean) -> Unit,
    onCompactSixRelativeChange: (Boolean) -> Unit,
    onShowHuiTouShengKeHintChange: (Boolean) -> Unit,
    onShowHuiTouChongHeHintChange: (Boolean) -> Unit,
    onShowDayMonthChongHeHintChange: (Boolean) -> Unit,
    onMarkBranchXunKongChange: (Boolean) -> Unit,
    onClickHighlightHintChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    SettingsPageScaffold(
        title = "排盘设置",
        onBackClick = onBackClick
    ) { innerPadding ->
        SettingsPageColumn(innerPadding = innerPadding) {
            SettingsGroup(title = "排盘设置") {
                SettingsSwitchRow(
                    title = "显示天干",
                    checked = showHeavenlyStem,
                    onCheckedChange = onShowHeavenlyStemChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "显示所有藏爻",
                    checked = showAllHiddenLines,
                    onCheckedChange = onShowAllHiddenLinesChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "显示简洁六神",
                    checked = compactSixGod,
                    onCheckedChange = onCompactSixGodChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "显示简洁六亲",
                    checked = compactSixRelative,
                    onCheckedChange = onCompactSixRelativeChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "回头生克提示",
                    checked = showHuiTouShengKeHint,
                    onCheckedChange = onShowHuiTouShengKeHintChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "回头冲合提示",
                    checked = showHuiTouChongHeHint,
                    onCheckedChange = onShowHuiTouChongHeHintChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "日月冲合提示",
                    checked = showDayMonthChongHeHint,
                    onCheckedChange = onShowDayMonthChongHeHintChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "地支旬空标记",
                    checked = markBranchXunKong,
                    onCheckedChange = onMarkBranchXunKongChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "点击高亮标记",
                    checked = clickHighlightHint,
                    onCheckedChange = onClickHighlightHintChange
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DivinationSettingsScreenPreview() {
    Wuchang_liuyaoTheme {
        DivinationSettingsScreen(
            compactSixGod = false,
            showHeavenlyStem = false,
            showAllHiddenLines = false,
            compactSixRelative = false,
            showHuiTouShengKeHint = false,
            showHuiTouChongHeHint = false,
            showDayMonthChongHeHint = false,
            markBranchXunKong = false,
            clickHighlightHint = false,
            onCompactSixGodChange = {},
            onShowHeavenlyStemChange = {},
            onShowAllHiddenLinesChange = {},
            onCompactSixRelativeChange = {},
            onShowHuiTouShengKeHintChange = {},
            onShowHuiTouChongHeHintChange = {},
            onShowDayMonthChongHeHintChange = {},
            onMarkBranchXunKongChange = {},
            onClickHighlightHintChange = {},
            onBackClick = {}
        )
    }
}
