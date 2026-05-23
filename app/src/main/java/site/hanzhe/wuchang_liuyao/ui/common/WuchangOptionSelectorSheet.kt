package site.hanzhe.wuchang_liuyao.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.hanzhe.wuchang_liuyao.ui.theme.AppTextHint
import site.hanzhe.wuchang_liuyao.ui.theme.LocalAppFontScale
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun <T> WuchangOptionSelectorSheet(
    options: List<T>,
    selectedOption: T,
    onDismiss: () -> Unit,
    onOptionSelected: (T) -> Unit,
    optionText: (T) -> String
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
                        val isSelected = option == selectedOption
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(option) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = optionText(option),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Medium
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (isSelected) "当前" else "选择",
                                color = if (isSelected) {
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
