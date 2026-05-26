package site.hanzhe.wuchang_liuyao.feature.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryGroup
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryRecord
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryRepository

internal data class HistoryUiState(
    val isLoading: Boolean = true,
    val groups: List<DivinationHistoryGroup> = emptyList(),
    val records: List<DivinationHistoryRecord> = emptyList(),
    val selectedGroupId: String? = null,
    val searchKeyword: String = "",
    val searchDraft: String = "",
    val isSearchDialogVisible: Boolean = false,
    val isRecordSelectionMode: Boolean = false,
    val selectedRecordIds: Set<String> = emptySet(),
    val isGroupSelectionMode: Boolean = false,
    val selectedGroupIds: Set<String> = emptySet(),
    val transientMessage: String? = null
)

internal class HistoryViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val historyRepository = DivinationHistoryRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val groups = historyRepository.getAllGroups()
                val records = historyRepository.getAllRecords()
                _uiState.update { currentState ->
                    val availableGroupIds = groups.map { group -> group.id }.toSet()
                    val selectedGroupId = currentState.selectedGroupId
                        ?.takeIf { groupId -> groupId in availableGroupIds }
                    currentState.copy(
                        isLoading = false,
                        groups = groups,
                        records = records,
                        selectedGroupId = selectedGroupId,
                        isRecordSelectionMode = currentState.isRecordSelectionMode && selectedGroupId != null,
                        selectedRecordIds = currentState.selectedRecordIds.intersect(
                            records.map { record -> record.id }.toSet()
                        ),
                        isGroupSelectionMode = currentState.isGroupSelectionMode && selectedGroupId == null,
                        selectedGroupIds = currentState.selectedGroupIds.intersect(
                            groups.map { group -> group.id }.toSet()
                        )
                    )
                }
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        groups = emptyList(),
                        records = emptyList(),
                        selectedGroupId = null,
                        isRecordSelectionMode = false,
                        selectedRecordIds = emptySet(),
                        isGroupSelectionMode = false,
                        selectedGroupIds = emptySet(),
                        transientMessage = "排盘记录加载失败"
                    )
                }
            }
        }
    }

    fun openSearchDialog() {
        _uiState.update {
            it.copy(
                isSearchDialogVisible = true,
                searchDraft = it.searchKeyword
            )
        }
    }

    fun updateSearchDraft(keyword: String) {
        _uiState.update { it.copy(searchDraft = keyword) }
    }

    fun confirmSearch() {
        _uiState.update {
            it.copy(
                searchKeyword = it.searchDraft.trim(),
                isSearchDialogVisible = false
            )
        }
    }

    fun dismissSearchDialog() {
        _uiState.update { it.copy(isSearchDialogVisible = false) }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchKeyword = "",
                searchDraft = "",
                isSearchDialogVisible = false
            )
        }
    }

    fun openGroup(groupId: String) {
        _uiState.update { currentState ->
            if (currentState.groups.none { group -> group.id == groupId }) {
                currentState
            } else {
                currentState.copy(
                    selectedGroupId = groupId,
                    searchKeyword = "",
                    searchDraft = "",
                    isSearchDialogVisible = false,
                    isGroupSelectionMode = false,
                    selectedGroupIds = emptySet(),
                    isRecordSelectionMode = false,
                    selectedRecordIds = emptySet()
                )
            }
        }
    }

    fun closeCurrentGroup() {
        _uiState.update {
            it.copy(
                selectedGroupId = null,
                searchKeyword = "",
                searchDraft = "",
                isSearchDialogVisible = false,
                isRecordSelectionMode = false,
                selectedRecordIds = emptySet()
            )
        }
    }

    fun enterGroupSelectionMode(groupId: String) {
        _uiState.update { currentState ->
            if (currentState.groups.none { group -> group.id == groupId }) {
                return@update currentState
            }
            currentState.copy(
                isGroupSelectionMode = true,
                selectedGroupIds = currentState.selectedGroupIds + groupId
            )
        }
    }

    fun toggleGroupSelection(groupId: String) {
        _uiState.update { currentState ->
            if (currentState.groups.none { group -> group.id == groupId }) {
                return@update currentState
            }
            val nextSelectedIds = if (groupId in currentState.selectedGroupIds) {
                currentState.selectedGroupIds - groupId
            } else {
                currentState.selectedGroupIds + groupId
            }
            currentState.copy(selectedGroupIds = nextSelectedIds)
        }
    }

    fun exitGroupSelectionMode() {
        _uiState.update {
            it.copy(
                isGroupSelectionMode = false,
                selectedGroupIds = emptySet()
            )
        }
    }

    fun toggleSelectAllGroups() {
        _uiState.update { currentState ->
            val selectableGroupIds = currentState.groups
                .map { group -> group.id }
                .toSet()
            val allSelected = selectableGroupIds.isNotEmpty() &&
                selectableGroupIds.all { groupId -> groupId in currentState.selectedGroupIds }
            currentState.copy(
                selectedGroupIds = if (allSelected) {
                    emptySet()
                } else {
                    selectableGroupIds
                }
            )
        }
    }

    fun createGroup(name: String): Boolean {
        val trimmedName = name.trim()
        validateGroupName(trimmedName, excludedGroupId = null)?.let { message ->
            _uiState.update { it.copy(transientMessage = message) }
            return false
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepository.createGroup(trimmedName)
                updateHistoryAfterGroupChange("新增成功")
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(transientMessage = throwable.message ?: "新增失败，请稍后重试")
                }
            }
        }
        return true
    }

    fun renameSelectedGroup(name: String): Boolean {
        val selectedGroup = selectedEditableGroupOrNull(
            emptyMessage = "请选择要重命名的分组",
            multipleMessage = "一次只能重命名一个分组"
        ) ?: return false
        val trimmedName = name.trim()
        validateGroupName(trimmedName, excludedGroupId = selectedGroup.id)?.let { message ->
            _uiState.update { it.copy(transientMessage = message) }
            return false
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepository.renameGroup(
                    groupId = selectedGroup.id,
                    name = trimmedName
                )
                updateHistoryAfterGroupChange("重命名成功")
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(transientMessage = throwable.message ?: "重命名失败，请稍后重试")
                }
            }
        }
        return true
    }

    fun moveSelectedGroupRecords(targetGroupId: String): Boolean {
        val selectedGroupIds = _uiState.value.selectedGroupIds
        if (selectedGroupIds.isEmpty()) {
            _uiState.update { it.copy(transientMessage = "请选择要移动的分组") }
            return false
        }
        if (selectedGroupIds.all { groupId -> groupId == targetGroupId }) {
            return false
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepository.moveRecordsToGroup(
                    sourceGroupIds = selectedGroupIds,
                    targetGroupId = targetGroupId
                )
                updateHistoryAfterGroupChange("移动成功")
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(transientMessage = throwable.message ?: "移动失败，请稍后重试")
                }
            }
        }
        return true
    }

    fun moveSelectedRecords(targetGroupId: String): Boolean {
        val selectedRecordIds = _uiState.value.selectedRecordIds
        if (selectedRecordIds.isEmpty()) {
            _uiState.update { it.copy(transientMessage = "请选择要移动的卦例") }
            return false
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepository.moveRecords(
                    recordIds = selectedRecordIds,
                    targetGroupId = targetGroupId
                )
                updateRecordsAfterDelete("移动成功")
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(transientMessage = throwable.message ?: "移动失败，请稍后重试")
                }
            }
        }
        return true
    }

    fun deleteSelectedGroups() {
        val selectedGroupIds = _uiState.value.selectedGroupIds
        val deletableGroupIds = _uiState.value.groups
            .filter { group -> group.id in selectedGroupIds && !group.isSystem }
            .map { group -> group.id }
            .toSet()
        if (selectedGroupIds.isEmpty()) {
            return
        }
        if (deletableGroupIds.isEmpty()) {
            _uiState.update { it.copy(transientMessage = "默认分组不能删除") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepository.deleteGroups(deletableGroupIds)
                updateHistoryAfterGroupChange("删除成功")
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(transientMessage = "删除失败，请稍后重试")
                }
            }
        }
    }

    fun moveGroupByOffset(groupId: String, offset: Int) {
        if (offset == 0) {
            return
        }
        var orderedGroupIds: List<String>? = null
        _uiState.update { currentState ->
            val currentIndex = currentState.groups.indexOfFirst { group -> group.id == groupId }
            if (currentIndex <= 0) {
                return@update currentState
            }
            val group = currentState.groups[currentIndex]
            if (group.isSystem) {
                return@update currentState
            }
            val targetIndex = (currentIndex + offset).coerceIn(1, currentState.groups.lastIndex)
            if (targetIndex == currentIndex) {
                return@update currentState
            }
            val nextGroups = currentState.groups.toMutableList().apply {
                removeAt(currentIndex)
                add(targetIndex, group)
            }.mapIndexed { index, currentGroup ->
                currentGroup.copy(sortOrder = index)
            }
            orderedGroupIds = nextGroups.map { currentGroup -> currentGroup.id }
            currentState.copy(groups = nextGroups)
        }
        orderedGroupIds?.let { groupIds ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    historyRepository.reorderGroups(groupIds)
                } catch (_: Throwable) {
                    _uiState.update {
                        it.copy(transientMessage = "分组排序保存失败")
                    }
                    refresh()
                }
            }
        }
    }

    fun enterSelectionMode(recordId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                isRecordSelectionMode = true,
                selectedRecordIds = currentState.selectedRecordIds + recordId
            )
        }
    }

    fun toggleRecordSelection(recordId: String) {
        _uiState.update { currentState ->
            val nextSelectedIds = if (recordId in currentState.selectedRecordIds) {
                currentState.selectedRecordIds - recordId
            } else {
                currentState.selectedRecordIds + recordId
            }
            currentState.copy(selectedRecordIds = nextSelectedIds)
        }
    }

    fun exitSelectionMode() {
        _uiState.update {
            it.copy(
                isRecordSelectionMode = false,
                selectedRecordIds = emptySet()
            )
        }
    }

    fun toggleSelectAllFilteredRecords() {
        _uiState.update { currentState ->
            val filteredIds = currentState.filteredRecords()
                .map { record -> record.id }
                .toSet()
            val allFilteredSelected = filteredIds.isNotEmpty() &&
                filteredIds.all { recordId -> recordId in currentState.selectedRecordIds }
            currentState.copy(
                selectedRecordIds = if (allFilteredSelected) {
                    currentState.selectedRecordIds - filteredIds
                } else {
                    currentState.selectedRecordIds + filteredIds
                }
            )
        }
    }

    fun invertFilteredRecordsSelection() {
        _uiState.update { currentState ->
            val filteredIds = currentState.filteredRecords()
                .map { record -> record.id }
                .toSet()
            val nextVisibleSelection = filteredIds - currentState.selectedRecordIds
            val hiddenSelection = currentState.selectedRecordIds - filteredIds
            currentState.copy(
                selectedRecordIds = hiddenSelection + nextVisibleSelection
            )
        }
    }

    fun deleteSelectedRecords() {
        val selectedRecordIds = _uiState.value.selectedRecordIds
        if (selectedRecordIds.isEmpty()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                historyRepository.deleteRecords(selectedRecordIds)
                updateRecordsAfterDelete("删除成功")
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(transientMessage = "删除失败，请稍后重试")
                }
            }
        }
    }

    fun consumeTransientMessage() {
        _uiState.update { currentState ->
            if (currentState.transientMessage == null) {
                currentState
            } else {
                currentState.copy(transientMessage = null)
            }
        }
    }

    private fun updateRecordsAfterDelete(message: String) {
        val groups = historyRepository.getAllGroups()
        val records = historyRepository.getAllRecords()
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                groups = groups,
                records = records,
                isRecordSelectionMode = false,
                selectedRecordIds = emptySet(),
                transientMessage = message
            )
        }
    }

    private fun updateHistoryAfterGroupChange(message: String) {
        val groups = historyRepository.getAllGroups()
        val records = historyRepository.getAllRecords()
        _uiState.update {
            it.copy(
                isLoading = false,
                groups = groups,
                records = records,
                selectedGroupId = null,
                isGroupSelectionMode = false,
                selectedGroupIds = emptySet(),
                isRecordSelectionMode = false,
                selectedRecordIds = emptySet(),
                transientMessage = message
            )
        }
    }

    private fun validateGroupName(
        name: String,
        excludedGroupId: String?
    ): String? {
        return when {
            name.isBlank() -> "分组名称不能为空"
            _uiState.value.groups.any { group ->
                group.id != excludedGroupId && group.name == name
            } -> "分组名称不能重复"
            else -> null
        }
    }

    private fun selectedEditableGroupOrNull(
        emptyMessage: String,
        multipleMessage: String
    ): DivinationHistoryGroup? {
        val currentState = _uiState.value
        return when (currentState.selectedGroupIds.size) {
            0 -> {
                _uiState.update { it.copy(transientMessage = emptyMessage) }
                null
            }

            1 -> {
                val groupId = currentState.selectedGroupIds.first()
                currentState.groups.firstOrNull { group -> group.id == groupId && !group.isSystem }
                    ?: run {
                        _uiState.update { it.copy(transientMessage = "默认分组不能重命名") }
                        null
                    }
            }

            else -> {
                _uiState.update { it.copy(transientMessage = multipleMessage) }
                null
            }
        }
    }
}

internal fun HistoryUiState.filteredRecords(): List<DivinationHistoryRecord> {
    val groupId = selectedGroupId ?: return emptyList()
    val keyword = searchKeyword.trim()
    val groupRecords = records.filter { record -> record.groupId == groupId }
    if (keyword.isBlank()) {
        return groupRecords
    }
    return groupRecords.filter { record ->
        record.displayQuestion().contains(keyword, ignoreCase = true)
    }
}

internal fun HistoryUiState.currentGroup(): DivinationHistoryGroup? {
    val groupId = selectedGroupId ?: return null
    return groups.firstOrNull { group -> group.id == groupId }
}

internal fun HistoryUiState.groupRecordCount(groupId: String): Int {
    return records.count { record -> record.groupId == groupId }
}

internal fun HistoryUiState.selectedEditableGroups(): List<DivinationHistoryGroup> {
    return groups.filter { group -> group.id in selectedGroupIds && !group.isSystem }
}

internal fun HistoryUiState.selectedGroups(): List<DivinationHistoryGroup> {
    return groups.filter { group -> group.id in selectedGroupIds }
}

internal fun DivinationHistoryRecord.displayQuestion(): String {
    return result.question.ifBlank { "无" }
}
