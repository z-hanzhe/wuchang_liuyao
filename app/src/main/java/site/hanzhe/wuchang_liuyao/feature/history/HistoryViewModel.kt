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
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryRecord
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryRepository

internal data class HistoryUiState(
    val isLoading: Boolean = true,
    val records: List<DivinationHistoryRecord> = emptyList(),
    val searchKeyword: String = "",
    val searchDraft: String = "",
    val isSearchDialogVisible: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedRecordIds: Set<String> = emptySet(),
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
                val records = historyRepository.getAllRecords()
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        records = records,
                        isSelectionMode = currentState.isSelectionMode,
                        selectedRecordIds = currentState.selectedRecordIds.intersect(
                            records.map { record -> record.id }.toSet()
                        )
                    )
                }
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        records = emptyList(),
                        isSelectionMode = false,
                        selectedRecordIds = emptySet(),
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

    fun enterSelectionMode(recordId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                isSelectionMode = true,
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
                isSelectionMode = false,
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
        val records = historyRepository.getAllRecords()
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                records = records,
                isSelectionMode = false,
                selectedRecordIds = emptySet(),
                transientMessage = message
            )
        }
    }
}

internal fun HistoryUiState.filteredRecords(): List<DivinationHistoryRecord> {
    val keyword = searchKeyword.trim()
    if (keyword.isBlank()) {
        return records
    }
    return records.filter { record ->
        record.displayQuestion().contains(keyword, ignoreCase = true)
    }
}

internal fun DivinationHistoryRecord.displayQuestion(): String {
    return result.question.ifBlank { "无" }
}
