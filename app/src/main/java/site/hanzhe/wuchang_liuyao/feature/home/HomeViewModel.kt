package site.hanzhe.wuchang_liuyao.feature.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onQuestionChange(question: String) {
        _uiState.update { it.copy(question = question) }
    }

    fun showDialogMessage(message: String) {
        _uiState.update { it.copy(dialogMessage = message) }
    }

    fun dismissDialogMessage() {
        _uiState.update { currentState ->
            if (currentState.dialogMessage == null) {
                currentState
            } else {
                currentState.copy(dialogMessage = null)
            }
        }
    }

    fun showMethodSheet() {
        _uiState.update { it.copy(isMethodSheetVisible = true) }
    }

    fun dismissMethodSheet() {
        _uiState.update { it.copy(isMethodSheetVisible = false) }
    }

    fun selectMethod(method: DivinationMethod) {
        _uiState.update { currentState ->
            if (currentState.selectedMethod == method) {
                currentState.copy(isMethodSheetVisible = false)
            } else {
                currentState.copy(
                    selectedMethod = method,
                    selectedYaoValues = emptySelectedValues(),
                    selectedCoinValues = emptySelectedValues(),
                    hexagramNameSelection = HexagramNameSelectionState(),
                    pointSelectionLines = defaultPointSelectionLines(),
                    onlineShakeValues = emptyOnlineShakeValues(),
                    onlineShakeCoinFaces = defaultOnlineShakeCoinFaces(),
                    nextOnlineShakeIndex = YaoNames.lastIndex,
                    isOnlineShakeAnimating = false,
                    isMethodSheetVisible = false,
                    activeYaoPicker = null
                )
            }
        }
    }

    fun resetCurrentMethodValues() {
        _uiState.update { currentState ->
            when (currentState.selectedMethod.inputSectionType) {
                InputSectionType.YAO_NAME -> currentState.copy(selectedYaoValues = emptySelectedValues())
                InputSectionType.HEXAGRAM_NAME -> currentState.copy(
                    hexagramNameSelection = HexagramNameSelectionState()
                )
                InputSectionType.COIN -> currentState.copy(selectedCoinValues = emptySelectedValues())
                InputSectionType.POINT_SELECT -> currentState.copy(
                    pointSelectionLines = defaultPointSelectionLines()
                )
                InputSectionType.ONLINE_SHAKE -> currentState.copy(
                    onlineShakeValues = emptyOnlineShakeValues(),
                    onlineShakeCoinFaces = defaultOnlineShakeCoinFaces(),
                    nextOnlineShakeIndex = YaoNames.lastIndex,
                    isOnlineShakeAnimating = false
                )
                null -> currentState
            }
        }
    }

    fun showYaoValueSheet(index: Int) {
        _uiState.update { currentState ->
            val sectionType = currentState.selectedMethod.inputSectionType ?: return@update currentState
            when (sectionType) {
                InputSectionType.YAO_NAME,
                InputSectionType.COIN -> {
                    currentState.copy(
                        activeYaoPicker = YaoPickerState(sectionType = sectionType, index = index)
                    )
                }

                InputSectionType.POINT_SELECT,
                InputSectionType.ONLINE_SHAKE,
                InputSectionType.HEXAGRAM_NAME -> currentState
            }
        }
    }

    fun dismissYaoValueSheet() {
        _uiState.update { it.copy(activeYaoPicker = null) }
    }

    fun selectYaoValue(value: String) {
        _uiState.update { currentState ->
            val pickerState = currentState.activeYaoPicker ?: return@update currentState
            when (pickerState.sectionType) {
                InputSectionType.YAO_NAME -> currentState.copy(
                    selectedYaoValues = currentState.selectedYaoValues.toMutableList().also {
                        it[pickerState.index] = value
                    },
                    activeYaoPicker = null
                )

                InputSectionType.COIN -> currentState.copy(
                    selectedCoinValues = currentState.selectedCoinValues.toMutableList().also {
                        it[pickerState.index] = value
                    },
                    activeYaoPicker = null
                )

                InputSectionType.POINT_SELECT -> currentState
                InputSectionType.ONLINE_SHAKE -> currentState
                InputSectionType.HEXAGRAM_NAME -> currentState
            }
        }
    }

    /** 更新卦名起卦中的本卦或变卦选择。 */
    fun selectHexagramTrigram(field: HexagramTrigramField, trigram: TrigramOption) {
        _uiState.update { currentState ->
            if (currentState.selectedMethod != DivinationMethod.HEXAGRAM_NAME) {
                return@update currentState
            }
            currentState.copy(
                hexagramNameSelection = currentState.hexagramNameSelection.withSelection(
                    field = field,
                    trigram = trigram
                )
            )
        }
    }

    fun togglePointSelectionLine(index: Int) {
        _uiState.update { currentState ->
            currentState.pointSelectionLines.getOrNull(index) ?: return@update currentState
            currentState.copy(
                pointSelectionLines = currentState.pointSelectionLines.toMutableList().also {
                    val currentLine = it[index]
                    it[index] = currentLine.copy(isYang = !currentLine.isYang)
                }
            )
        }
    }

    fun setPointSelectionLineMoving(index: Int, isMoving: Boolean) {
        _uiState.update { currentState ->
            currentState.pointSelectionLines.getOrNull(index) ?: return@update currentState
            currentState.copy(
                pointSelectionLines = currentState.pointSelectionLines.toMutableList().also {
                    it[index] = it[index].copy(isMoving = isMoving)
                }
            )
        }
    }

    fun toggleOnlineShake() {
        _uiState.update { currentState ->
            if (currentState.selectedMethod != DivinationMethod.MANUAL) {
                return@update currentState
            }
            if (currentState.nextOnlineShakeIndex < 0) {
                return@update currentState
            }
            if (!currentState.isOnlineShakeAnimating) {
                return@update currentState.copy(isOnlineShakeAnimating = true)
            }

            val randomFaces = randomOnlineShakeCoinFaces()
            val generatedValue = resolveOnlineShakeYaoValue(randomFaces)

            currentState.copy(
                onlineShakeValues = currentState.onlineShakeValues.toMutableList().also {
                    it[currentState.nextOnlineShakeIndex] = generatedValue
                },
                onlineShakeCoinFaces = randomFaces,
                nextOnlineShakeIndex = currentState.nextOnlineShakeIndex - 1,
                isOnlineShakeAnimating = false
            )
        }
    }
}
