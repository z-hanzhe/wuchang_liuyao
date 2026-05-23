package site.hanzhe.wuchang_liuyao.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

@Composable
internal fun SettingsScreen(
    onGlobalSettingsClick: () -> Unit,
    onDivinationSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    SettingsPageScaffold(
        title = "设置",
        onBackClick = onBackClick
    ) { innerPadding ->
        SettingsPageColumn(innerPadding = innerPadding) {
            SettingsGroup(title = "软件设置") {
                SettingsLinkRow(
                    title = "通用设置",
                    onClick = onGlobalSettingsClick
                )
                SettingsDivider()
                SettingsLinkRow(
                    title = "排盘设置",
                    onClick = onDivinationSettingsClick
                )
            }
            SettingsGroup(title = "更多功能") {
                SettingsLinkRow(
                    title = "排盘记录",
                    onClick = onHistoryClick
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    Wuchang_liuyaoTheme {
        SettingsScreen(
            onGlobalSettingsClick = {},
            onDivinationSettingsClick = {},
            onHistoryClick = {},
            onBackClick = {}
        )
    }
}
