package site.hanzhe.wuchang_liuyao.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import site.hanzhe.wuchang_liuyao.ui.theme.LocalAppFontScale
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

@Composable
internal fun <T> WuchangOptionPickerDialog(
    title: String,
    options: List<T>,
    selectedOptionId: String,
    onDismiss: () -> Unit,
    onOptionSelected: (T) -> Unit,
    optionId: (T) -> String,
    optionText: (T) -> String,
    itemVerticalPadding: Dp = 11.dp
) {
    val selectedIndex = options.indexOfFirst { optionId(it) == selectedOptionId }
    val listState = rememberLazyListState()
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.3f)
    val appFontScale = LocalAppFontScale.current

    LaunchedEffect(selectedOptionId, options) {
        if (selectedIndex >= 0) {
            listState.scrollToItem(selectedIndex)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Wuchang_liuyaoTheme(appFontScale = appFontScale) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        state = listState
                    ) {
                        itemsIndexed(
                            items = options,
                            key = { _, item -> optionId(item) }
                        ) { index, option ->
                            val isSelected = optionId(option) == selectedOptionId
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .noRippleClick(onClick = { onOptionSelected(option) })
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = itemVerticalPadding * fontScale
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = optionText(option),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "已选",
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                if (index < options.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "关闭",
                        modifier = Modifier
                            .align(Alignment.End)
                            .noRippleClick(onClick = onDismiss),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
