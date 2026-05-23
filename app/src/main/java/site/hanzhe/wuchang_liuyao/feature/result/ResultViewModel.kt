package site.hanzhe.wuchang_liuyao.feature.result

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
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationRequest
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationResult
import site.hanzhe.wuchang_liuyao.domain.divination.HexagramCalculator

internal sealed interface ResultGenerationStatus {
    data object Success : ResultGenerationStatus
    data class Failure(val message: String) : ResultGenerationStatus
}

internal data class ResultEditDraft(
    val question: String = "",
    val currentSituation: String = "",
    val judgment: String = ""
)

internal data class ResultUiState(
    val result: DivinationResult? = null,
    val request: DivinationRequest? = null,
    val savedRecordId: String? = null,
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val currentSituation: String = "",
    val judgment: String = "",
    val editDraft: ResultEditDraft? = null,
    val transientMessage: String? = null
) {
    val showSaveAction: Boolean
        get() = result != null && request != null && !isSaved && !isSaving

    val showEditAction: Boolean
        get() = result != null && request != null && isSaved && savedRecordId != null && !isSaving
}

internal class ResultViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val calculator = HexagramCalculator()
    private val historyRepository = DivinationHistoryRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun showResult(request: DivinationRequest): ResultGenerationStatus {
        return try {
            val result = calculator.calculate(request)
            _uiState.update {
                it.copy(
                    result = result,
                    request = request,
                    savedRecordId = null,
                    isSaved = false,
                    isSaving = false,
                    currentSituation = "",
                    judgment = "",
                    editDraft = null,
                    transientMessage = null
                )
            }
            ResultGenerationStatus.Success
        } catch (throwable: IllegalArgumentException) {
            ResultGenerationStatus.Failure(
                message = throwable.message ?: "排盘失败，请检查输入"
            )
        }
    }

    fun showSavedResult(record: DivinationHistoryRecord) {
        _uiState.update {
            it.copy(
                result = record.result,
                request = record.request,
                savedRecordId = record.id,
                isSaved = true,
                isSaving = false,
                currentSituation = record.currentSituation,
                judgment = record.judgment,
                editDraft = null,
                transientMessage = null
            )
        }
    }

    fun saveCurrentResult() {
        val currentState = _uiState.value
        val request = currentState.request ?: return
        val result = currentState.result ?: return
        if (currentState.isSaved || currentState.isSaving) {
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val record = historyRepository.saveRecord(
                    request = request,
                    result = result
                )
                _uiState.update {
                    it.copy(
                        savedRecordId = record.id,
                        isSaved = true,
                        isSaving = false,
                        currentSituation = record.currentSituation,
                        judgment = record.judgment,
                        transientMessage = "保存成功"
                    )
                }
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        transientMessage = "保存失败，请稍后重试"
                    )
                }
            }
        }
    }

    fun openEditDialog() {
        _uiState.update { currentState ->
            val result = currentState.result ?: return@update currentState
            if (!currentState.isSaved || currentState.savedRecordId == null) {
                return@update currentState
            }
            currentState.copy(
                editDraft = ResultEditDraft(
                    question = result.question,
                    currentSituation = currentState.currentSituation,
                    judgment = currentState.judgment
                )
            )
        }
    }

    fun dismissEditDialog() {
        _uiState.update { it.copy(editDraft = null) }
    }

    fun updateEditQuestion(question: String) {
        _uiState.update { currentState ->
            val editDraft = currentState.editDraft ?: return@update currentState
            currentState.copy(editDraft = editDraft.copy(question = question))
        }
    }

    fun updateEditCurrentSituation(currentSituation: String) {
        _uiState.update { currentState ->
            val editDraft = currentState.editDraft ?: return@update currentState
            currentState.copy(editDraft = editDraft.copy(currentSituation = currentSituation))
        }
    }

    fun updateEditJudgment(judgment: String) {
        _uiState.update { currentState ->
            val editDraft = currentState.editDraft ?: return@update currentState
            currentState.copy(editDraft = editDraft.copy(judgment = judgment))
        }
    }

    fun saveEditDraft() {
        val currentState = _uiState.value
        val recordId = currentState.savedRecordId ?: return
        val editDraft = currentState.editDraft ?: return
        if (currentState.isSaving) {
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedRecord = historyRepository.updateRecord(
                    recordId = recordId,
                    question = editDraft.question,
                    currentSituation = editDraft.currentSituation,
                    judgment = editDraft.judgment
                )
                _uiState.update {
                    it.copy(
                        result = updatedRecord.result,
                        request = updatedRecord.request,
                        isSaving = false,
                        currentSituation = updatedRecord.currentSituation,
                        judgment = updatedRecord.judgment,
                        editDraft = null,
                        transientMessage = "保存成功"
                    )
                }
            } catch (_: Throwable) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        transientMessage = "保存失败，请稍后重试"
                    )
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
}
