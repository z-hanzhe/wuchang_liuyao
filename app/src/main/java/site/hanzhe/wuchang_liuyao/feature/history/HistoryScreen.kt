package site.hanzhe.wuchang_liuyao.feature.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryRecord
import site.hanzhe.wuchang_liuyao.ui.common.WuchangDialogActionButton
import site.hanzhe.wuchang_liuyao.ui.common.WuchangDialogTitle
import site.hanzhe.wuchang_liuyao.ui.common.TopBarTextAction
import site.hanzhe.wuchang_liuyao.ui.common.WuchangConfirmDialog
import site.hanzhe.wuchang_liuyao.ui.common.WuchangSingleLineInput
import site.hanzhe.wuchang_liuyao.ui.common.WuchangTopAppBar
import site.hanzhe.wuchang_liuyao.ui.common.noRippleClick

@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onRecordClick: (DivinationHistoryRecord) -> Unit,
    onRecordLongClick: (String) -> Unit,
    onRecordSelectionToggle: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSearchDraftChange: (String) -> Unit,
    onSearchConfirm: () -> Unit,
    onSearchDismiss: () -> Unit,
    onSelectAllClick: () -> Unit,
    onInvertSelectionClick: () -> Unit,
    onDeleteSelectedClick: () -> Unit,
    onExitSelectionMode: () -> Unit,
    onBackClick: () -> Unit
) {
    val filteredRecords = remember(uiState.records, uiState.searchKeyword) {
        uiState.filteredRecords()
    }
    var isDeleteConfirmVisible by remember(uiState.selectedRecordIds) { mutableStateOf(false) }

    BackHandler {
        if (uiState.isSelectionMode) {
            onExitSelectionMode()
        } else {
            onBackClick()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            HistoryTopAppBar(
                selectedCount = uiState.selectedRecordIds.size,
                isSelectionMode = uiState.isSelectionMode,
                onSearchClick = onSearchClick,
                onBackClick = {
                    if (uiState.isSelectionMode) {
                        onExitSelectionMode()
                    } else {
                        onBackClick()
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.isSelectionMode) {
                HistorySelectionBar(
                    onSelectAllClick = onSelectAllClick,
                    onInvertSelectionClick = onInvertSelectionClick,
                    onDeleteSelectedClick = {
                        if (uiState.selectedRecordIds.isNotEmpty()) {
                            isDeleteConfirmVisible = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                HistoryPlaceholder(
                    innerPadding = innerPadding,
                    text = "正在加载排盘记录..."
                )
            }

            filteredRecords.isEmpty() -> {
                HistoryPlaceholder(
                    innerPadding = innerPadding,
                    text = if (uiState.searchKeyword.isBlank()) {
                        "暂无排盘记录"
                    } else {
                        "未找到相关记录"
                    }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 12.dp,
                            top = innerPadding.calculateTopPadding() + 2.dp,
                            end = 12.dp,
                            bottom = innerPadding.calculateBottomPadding() + 2.dp
                        )
                ) {
                    itemsIndexed(
                        items = filteredRecords,
                        key = { _, record -> record.id }
                    ) { index: Int, record: DivinationHistoryRecord ->
                        Column {
                            HistoryRecordItem(
                                record = record,
                                isSelectionMode = uiState.isSelectionMode,
                                isSelected = record.id in uiState.selectedRecordIds,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        onRecordSelectionToggle(record.id)
                                    } else {
                                        onRecordClick(record)
                                    }
                                },
                                onLongClick = {
                                    if (uiState.isSelectionMode) {
                                        onRecordSelectionToggle(record.id)
                                    } else {
                                        onRecordLongClick(record.id)
                                    }
                                },
                                onCheckboxClick = { onRecordSelectionToggle(record.id) }
                            )
                            if (index < filteredRecords.lastIndex) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.isSearchDialogVisible) {
        HistorySearchDialog(
            keyword = uiState.searchDraft,
            onKeywordChange = onSearchDraftChange,
            onDismiss = onSearchDismiss,
            onConfirm = onSearchConfirm
        )
    }

    if (isDeleteConfirmVisible) {
        WuchangConfirmDialog(
            title = "删除确认",
            message = "已选中的排盘记录将被删除，是否继续",
            confirmText = "确认删除",
            onConfirm = {
                isDeleteConfirmVisible = false
                onDeleteSelectedClick()
            },
            onDismiss = { isDeleteConfirmVisible = false }
        )
    }
}

@Composable
private fun HistoryTopAppBar(
    selectedCount: Int,
    isSelectionMode: Boolean,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit
) {
    WuchangTopAppBar(
        title = if (isSelectionMode) {
            "已选 $selectedCount 项"
        } else {
            "排盘记录"
        },
        onBackClick = onBackClick,
        actions = {
            if (!isSelectionMode) {
                TopBarTextAction(
                    text = "搜索",
                    onClick = onSearchClick
                )
            }
        }
    )
}

@Composable
private fun HistoryPlaceholder(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRecordItem(
    record: DivinationHistoryRecord,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckboxClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 2.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onCheckboxClick() }
            )
            Box(modifier = Modifier.width(6.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = record.displayQuestion(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(formatHistoryCreatedTime(record.createdAtMillis))
                    append("  ")
                    append(record.result.baseHexagramName)
                    record.result.changedHexagramName
                        ?.takeIf { it.isNotBlank() }
                        ?.let { changedName ->
                            append(" -> ")
                            append(changedName)
                        }
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HistorySelectionBar(
    onSelectAllClick: () -> Unit,
    onInvertSelectionClick: () -> Unit,
    onDeleteSelectedClick: () -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        top = 8.dp,
                        end = 12.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 10.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryBottomAction(
                    modifier = Modifier.weight(1f),
                    text = "全选",
                    onClick = onSelectAllClick
                )
                HistoryBottomAction(
                    modifier = Modifier.weight(1f),
                    text = "反选",
                    onClick = onInvertSelectionClick
                )
                HistoryBottomAction(
                    modifier = Modifier.weight(1f),
                    text = "删除",
                    isDanger = true,
                    onClick = onDeleteSelectedClick
                )
            }
        }
    }
}

@Composable
private fun HistoryBottomAction(
    modifier: Modifier = Modifier,
    text: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDanger) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .border(
                width = 1.dp,
                color = if (isDanger) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .noRippleClick(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isDanger) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HistorySearchDialog(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
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
                WuchangDialogTitle(text = "搜索排盘记录")
                WuchangSingleLineInput(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    placeholder = "请输入问念关键字",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                )
                Row(
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
                        text = "确定",
                        isPrimary = true,
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}

private fun formatHistoryCreatedTime(createdAtMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    return formatter.format(Date(createdAtMillis))
}
