package site.hanzhe.wuchang_liuyao.feature.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryGroup
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryRecord
import site.hanzhe.wuchang_liuyao.ui.common.TopBarTextAction
import site.hanzhe.wuchang_liuyao.ui.common.WuchangConfirmDialog
import site.hanzhe.wuchang_liuyao.ui.common.WuchangDialogActionButton
import site.hanzhe.wuchang_liuyao.ui.common.WuchangDialogTitle
import site.hanzhe.wuchang_liuyao.ui.common.WuchangSingleLineInput
import site.hanzhe.wuchang_liuyao.ui.common.WuchangTopAppBar
import site.hanzhe.wuchang_liuyao.ui.common.noRippleClick
import site.hanzhe.wuchang_liuyao.ui.theme.LocalAppFontScale
import site.hanzhe.wuchang_liuyao.ui.theme.Wuchang_liuyaoTheme

@Composable
internal fun HistoryScreen(
    uiState: HistoryUiState,
    onRecordClick: (DivinationHistoryRecord) -> Unit,
    onRecordLongClick: (String) -> Unit,
    onRecordSelectionToggle: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onGroupLongClick: (String) -> Unit,
    onGroupSelectionToggle: (String) -> Unit,
    onGroupMove: (String, Int) -> Unit,
    onAddGroupConfirm: (String) -> Boolean,
    onRenameGroupConfirm: (String) -> Boolean,
    onMoveGroupRecordsConfirm: (String) -> Boolean,
    onMoveSelectedRecordsConfirm: (String) -> Boolean,
    onDeleteSelectedGroupsConfirm: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchDraftChange: (String) -> Unit,
    onSearchConfirm: () -> Unit,
    onSearchDismiss: () -> Unit,
    onSearchClear: () -> Unit,
    onSelectAllGroupsClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onInvertSelectionClick: () -> Unit,
    onDeleteSelectedClick: () -> Unit,
    onExitGroupSelectionMode: () -> Unit,
    onExitSelectionMode: () -> Unit,
    onCloseCurrentGroup: () -> Unit,
    onBackClick: () -> Unit
) {
    val filteredRecords = remember(uiState.records, uiState.selectedGroupId, uiState.searchKeyword) {
        uiState.filteredRecords()
    }
    val currentGroup = uiState.currentGroup()
    var isDeleteRecordsConfirmVisible by remember(uiState.selectedRecordIds) { mutableStateOf(false) }
    var isAddGroupDialogVisible by remember { mutableStateOf(false) }
    var isRenameGroupDialogVisible by remember(uiState.selectedGroupIds) { mutableStateOf(false) }
    var isMoveGroupDialogVisible by remember(uiState.selectedGroupIds) { mutableStateOf(false) }
    var isMoveRecordsDialogVisible by remember(uiState.selectedRecordIds) { mutableStateOf(false) }
    var isDeleteGroupsConfirmVisible by remember(uiState.selectedGroupIds) { mutableStateOf(false) }
    val hasSearchResult = currentGroup != null && uiState.searchKeyword.isNotBlank()

    BackHandler {
        when {
            uiState.isGroupSelectionMode -> onExitGroupSelectionMode()
            uiState.isRecordSelectionMode -> onExitSelectionMode()
            hasSearchResult -> onSearchClear()
            currentGroup != null -> onCloseCurrentGroup()
            else -> onBackClick()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            HistoryTopAppBar(
                title = resolveHistoryTitle(uiState),
                isGroupPage = currentGroup == null,
                isGroupSelectionMode = uiState.isGroupSelectionMode,
                isRecordSelectionMode = uiState.isRecordSelectionMode,
                onSearchClick = onSearchClick,
                onAddGroupClick = { isAddGroupDialogVisible = true },
                onBackClick = {
                    when {
                        uiState.isGroupSelectionMode -> onExitGroupSelectionMode()
                        uiState.isRecordSelectionMode -> onExitSelectionMode()
                        hasSearchResult -> onSearchClear()
                        currentGroup != null -> onCloseCurrentGroup()
                        else -> onBackClick()
                    }
                }
            )
        },
        bottomBar = {
            when {
                uiState.isGroupSelectionMode -> {
                    GroupSelectionBar(
                        onSelectAllClick = onSelectAllGroupsClick,
                        onRenameClick = {
                            if (uiState.selectedEditableGroups().size == 1) {
                                isRenameGroupDialogVisible = true
                            } else {
                                onRenameGroupConfirm("")
                            }
                        },
                        onMoveClick = {
                            if (uiState.selectedGroupIds.isNotEmpty()) {
                                isMoveGroupDialogVisible = true
                            } else {
                                onMoveGroupRecordsConfirm("")
                            }
                        },
                        onDeleteClick = {
                            if (uiState.selectedEditableGroups().isNotEmpty()) {
                                isDeleteGroupsConfirmVisible = true
                            } else if (uiState.selectedGroupIds.isNotEmpty()) {
                                onDeleteSelectedGroupsConfirm()
                            }
                        }
                    )
                }

                uiState.isRecordSelectionMode -> {
                    RecordSelectionBar(
                        onSelectAllClick = onSelectAllClick,
                        onInvertSelectionClick = onInvertSelectionClick,
                        onMoveClick = {
                            if (uiState.selectedRecordIds.isNotEmpty()) {
                                isMoveRecordsDialogVisible = true
                            }
                        },
                        onDeleteSelectedClick = {
                            if (uiState.selectedRecordIds.isNotEmpty()) {
                                isDeleteRecordsConfirmVisible = true
                            }
                        }
                    )
                }
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

            currentGroup == null -> {
                GroupListContent(
                    uiState = uiState,
                    innerPadding = innerPadding,
                    onGroupClick = { group ->
                        if (uiState.isGroupSelectionMode) {
                            onGroupSelectionToggle(group.id)
                        } else {
                            onGroupClick(group.id)
                        }
                    },
                    onGroupLongClick = { group ->
                        if (uiState.isGroupSelectionMode) {
                            onGroupSelectionToggle(group.id)
                        } else {
                            onGroupLongClick(group.id)
                        }
                    },
                    onGroupCheckboxClick = { group ->
                        onGroupSelectionToggle(group.id)
                    },
                    onGroupMove = onGroupMove
                )
            }

            filteredRecords.isEmpty() -> {
                HistoryRecordsContent(
                    records = emptyList(),
                    uiState = uiState,
                    innerPadding = innerPadding,
                    showSearchResultHeader = hasSearchResult,
                    onSearchClear = onSearchClear,
                    onRecordClick = onRecordClick,
                    onRecordLongClick = onRecordLongClick,
                    onRecordSelectionToggle = onRecordSelectionToggle
                )
            }

            else -> {
                HistoryRecordsContent(
                    records = filteredRecords,
                    uiState = uiState,
                    innerPadding = innerPadding,
                    showSearchResultHeader = hasSearchResult,
                    onSearchClear = onSearchClear,
                    onRecordClick = onRecordClick,
                    onRecordLongClick = onRecordLongClick,
                    onRecordSelectionToggle = onRecordSelectionToggle
                )
            }
        }
    }

    if (isAddGroupDialogVisible) {
        GroupNameDialog(
            title = "新增分组",
            initialName = "",
            confirmText = "新增",
            onDismiss = { isAddGroupDialogVisible = false },
            onConfirm = { name ->
                if (onAddGroupConfirm(name)) {
                    isAddGroupDialogVisible = false
                }
            }
        )
    }

    if (isRenameGroupDialogVisible) {
        val selectedGroup = uiState.selectedEditableGroups().singleOrNull()
        if (selectedGroup != null) {
            GroupNameDialog(
                title = "重命名分组",
                initialName = selectedGroup.name,
                confirmText = "保存",
                onDismiss = { isRenameGroupDialogVisible = false },
                onConfirm = { name ->
                    if (onRenameGroupConfirm(name)) {
                        isRenameGroupDialogVisible = false
                    }
                }
            )
        }
    }

    if (isMoveGroupDialogVisible) {
        val selectedGroups = uiState.selectedGroups()
        if (selectedGroups.isNotEmpty()) {
            MoveTargetGroupDialog(
                title = "移动至",
                targetGroups = uiState.groups.filterNot { group ->
                    group.id in selectedGroups.map { selectedGroup -> selectedGroup.id }.toSet()
                },
                confirmMessage = { targetGroup ->
                    val sourceNames = selectedGroups.joinToString("、") { group -> group.name }
                    "确认将${sourceNames}分组下所有卦例移动至${targetGroup.name}分组？"
                },
                onDismiss = { isMoveGroupDialogVisible = false },
                onConfirm = { targetGroup ->
                    if (onMoveGroupRecordsConfirm(targetGroup.id)) {
                        isMoveGroupDialogVisible = false
                    }
                }
            )
        }
    }

    if (isMoveRecordsDialogVisible) {
        MoveTargetGroupDialog(
            title = "移动至",
            targetGroups = uiState.groups.filter { group -> group.id != uiState.selectedGroupId },
            confirmMessage = { targetGroup ->
                "确认将选中的${uiState.selectedRecordIds.size}个卦移动至${targetGroup.name}分组？"
            },
            onDismiss = { isMoveRecordsDialogVisible = false },
            onConfirm = { targetGroup ->
                if (onMoveSelectedRecordsConfirm(targetGroup.id)) {
                    isMoveRecordsDialogVisible = false
                }
            }
        )
    }

    if (isDeleteGroupsConfirmVisible) {
        val selectedGroups = uiState.selectedEditableGroups()
        val groupNames = selectedGroups.joinToString("、") { group -> group.name }
        val recordCount = selectedGroups.sumOf { group -> uiState.groupRecordCount(group.id) }
        WuchangConfirmDialog(
            title = "删除确认",
            message = "${groupNames}分组下有${recordCount}个卦例，确认删除吗？",
            confirmText = "确认删除",
            onConfirm = {
                isDeleteGroupsConfirmVisible = false
                onDeleteSelectedGroupsConfirm()
            },
            onDismiss = { isDeleteGroupsConfirmVisible = false }
        )
    }

    if (uiState.isSearchDialogVisible) {
        HistorySearchDialog(
            keyword = uiState.searchDraft,
            onKeywordChange = onSearchDraftChange,
            onDismiss = onSearchDismiss,
            onConfirm = onSearchConfirm
        )
    }

    if (isDeleteRecordsConfirmVisible) {
        WuchangConfirmDialog(
            title = "删除确认",
            message = "已选中的排盘记录将被删除，是否继续",
            confirmText = "确认删除",
            onConfirm = {
                isDeleteRecordsConfirmVisible = false
                onDeleteSelectedClick()
            },
            onDismiss = { isDeleteRecordsConfirmVisible = false }
        )
    }
}

@Composable
private fun HistoryTopAppBar(
    title: String,
    isGroupPage: Boolean,
    isGroupSelectionMode: Boolean,
    isRecordSelectionMode: Boolean,
    onSearchClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onBackClick: () -> Unit
) {
    WuchangTopAppBar(
        title = title,
        onBackClick = onBackClick,
        actions = {
            when {
                isGroupPage && !isGroupSelectionMode -> {
                    TopBarTextAction(
                        text = "新增",
                        onClick = onAddGroupClick
                    )
                }

                !isGroupSelectionMode && !isRecordSelectionMode -> {
                    TopBarTextAction(
                        text = "搜索",
                        onClick = onSearchClick
                    )
                }
            }
        }
    )
}

private fun resolveHistoryTitle(uiState: HistoryUiState): String {
    return when {
        uiState.isGroupSelectionMode -> "已选 ${uiState.selectedGroupIds.size} 项"
        uiState.isRecordSelectionMode -> "已选 ${uiState.selectedRecordIds.size} 项"
        uiState.currentGroup() != null -> uiState.currentGroup()?.name.orEmpty()
        else -> "排盘记录"
    }
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

@Composable
private fun GroupListContent(
    uiState: HistoryUiState,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onGroupClick: (DivinationHistoryGroup) -> Unit,
    onGroupLongClick: (DivinationHistoryGroup) -> Unit,
    onGroupCheckboxClick: (DivinationHistoryGroup) -> Unit,
    onGroupMove: (String, Int) -> Unit
) {
    if (uiState.groups.isEmpty()) {
        HistoryPlaceholder(
            innerPadding = innerPadding,
            text = "暂无分组"
        )
        return
    }

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
            items = uiState.groups,
            key = { _, group -> group.id }
        ) { index, group ->
            Column {
                HistoryGroupItem(
                    group = group,
                    recordCount = uiState.groupRecordCount(group.id),
                    isSelectionMode = uiState.isGroupSelectionMode,
                    isSelected = group.id in uiState.selectedGroupIds,
                    onClick = { onGroupClick(group) },
                    onLongClick = { onGroupLongClick(group) },
                    onCheckboxClick = { onGroupCheckboxClick(group) },
                    onMove = { offset -> onGroupMove(group.id, offset) }
                )
                if (index < uiState.groups.lastIndex) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordsContent(
    records: List<DivinationHistoryRecord>,
    uiState: HistoryUiState,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    showSearchResultHeader: Boolean,
    onSearchClear: () -> Unit,
    onRecordClick: (DivinationHistoryRecord) -> Unit,
    onRecordLongClick: (String) -> Unit,
    onRecordSelectionToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 12.dp,
                top = innerPadding.calculateTopPadding() + 2.dp,
                end = 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 2.dp
            )
    ) {
        if (showSearchResultHeader) {
            SearchResultHeader(
                keyword = uiState.searchKeyword,
                onClear = onSearchClear
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
            )
        }
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showSearchResultHeader) "未找到相关记录" else "暂无排盘记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = records,
                    key = { _, record -> record.id }
                ) { index: Int, record: DivinationHistoryRecord ->
                    Column {
                        HistoryRecordItem(
                            record = record,
                            isSelectionMode = uiState.isRecordSelectionMode,
                            isSelected = record.id in uiState.selectedRecordIds,
                            onClick = {
                                if (uiState.isRecordSelectionMode) {
                                    onRecordSelectionToggle(record.id)
                                } else {
                                    onRecordClick(record)
                                }
                            },
                            onLongClick = {
                                if (uiState.isRecordSelectionMode) {
                                    onRecordSelectionToggle(record.id)
                                } else {
                                    onRecordLongClick(record.id)
                                }
                            },
                            onCheckboxClick = { onRecordSelectionToggle(record.id) }
                        )
                        if (index < records.lastIndex) {
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

@Composable
private fun SearchResultHeader(
    keyword: String,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 2.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${keyword} 搜索结果：",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "取消搜索",
            modifier = Modifier.noRippleClick(onClick = onClear),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryGroupItem(
    group: DivinationHistoryGroup,
    recordCount: Int,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    onMove: (Int) -> Unit
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
                text = group.name,
                color = if (group.isSystem) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${recordCount} 个卦例",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
        if (isSelectionMode && !group.isSystem) {
            GroupDragHandle(onMove = onMove)
        }
    }
}

@Composable
private fun GroupDragHandle(
    onMove: (Int) -> Unit
) {
    val rowMoveThreshold = 42.dp
    val thresholdPx = with(LocalDensity.current) { rowMoveThreshold.toPx() }
    var accumulatedDrag by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 44.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        accumulatedDrag = 0f
                    },
                    onDragEnd = {
                        accumulatedDrag = 0f
                    },
                    onDragCancel = {
                        accumulatedDrag = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount.y
                        if (abs(accumulatedDrag) >= thresholdPx) {
                            val offset = if (accumulatedDrag > 0f) 1 else -1
                            onMove(offset)
                            accumulatedDrag = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f))
                )
            }
        }
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
private fun GroupSelectionBar(
    onSelectAllClick: () -> Unit,
    onRenameClick: () -> Unit,
    onMoveClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                    text = "重命名",
                    onClick = onRenameClick
                )
                HistoryBottomAction(
                    modifier = Modifier.weight(1f),
                    text = "移动至",
                    onClick = onMoveClick
                )
                HistoryBottomAction(
                    modifier = Modifier.weight(1f),
                    text = "删除",
                    isDanger = true,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun RecordSelectionBar(
    onSelectAllClick: () -> Unit,
    onInvertSelectionClick: () -> Unit,
    onMoveClick: () -> Unit,
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
                    text = "移动",
                    onClick = onMoveClick
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
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GroupNameDialog(
    title: String,
    initialName: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    val appFontScale = LocalAppFontScale.current

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
                    WuchangDialogTitle(text = title)
                    WuchangSingleLineInput(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "请输入分组名称",
                        modifier = Modifier.fillMaxWidth()
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
                            text = confirmText,
                            isPrimary = true,
                            onClick = { onConfirm(name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveTargetGroupDialog(
    title: String,
    targetGroups: List<DivinationHistoryGroup>,
    confirmMessage: (DivinationHistoryGroup) -> String,
    onDismiss: () -> Unit,
    onConfirm: (DivinationHistoryGroup) -> Unit
) {
    var pendingTargetGroup by remember { mutableStateOf<DivinationHistoryGroup?>(null) }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WuchangDialogTitle(text = title)
                if (targetGroups.isEmpty()) {
                    Text(
                        text = "暂无可移动的目标分组",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        itemsIndexed(
                            items = targetGroups,
                            key = { _, group -> group.id }
                        ) { index, group ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .noRippleClick { pendingTargetGroup = group }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.name,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "选择",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (index < targetGroups.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    WuchangDialogActionButton(
                        modifier = Modifier.width(96.dp),
                        text = "取消",
                        isPrimary = false,
                        onClick = onDismiss
                    )
                }
            }
        }
    }

    pendingTargetGroup?.let { targetGroup ->
        WuchangConfirmDialog(
            title = "移动确认",
            message = confirmMessage(targetGroup),
            confirmText = "确认移动",
            onConfirm = {
                pendingTargetGroup = null
                onConfirm(targetGroup)
            },
            onDismiss = { pendingTargetGroup = null }
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
    val appFontScale = LocalAppFontScale.current

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
                        modifier = Modifier.fillMaxWidth()
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
}

private fun formatHistoryCreatedTime(createdAtMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    return formatter.format(Date(createdAtMillis))
}
