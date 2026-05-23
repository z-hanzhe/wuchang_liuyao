package site.hanzhe.wuchang_liuyao.feature.time

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import site.hanzhe.wuchang_liuyao.data.calendar.CalendarQueryResult
import site.hanzhe.wuchang_liuyao.data.calendar.CalendarRecord
import site.hanzhe.wuchang_liuyao.data.calendar.CnCalendarRepository
import site.hanzhe.wuchang_liuyao.data.calendar.buildGanzhiSummary
import site.hanzhe.wuchang_liuyao.data.calendar.toCalendarSummary
import site.hanzhe.wuchang_liuyao.data.calendar.toLunarDate
import site.hanzhe.wuchang_liuyao.domain.time.CalendarSummary
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTime
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.domain.time.LunarDate
import site.hanzhe.wuchang_liuyao.domain.time.SolarDateTime
import site.hanzhe.wuchang_liuyao.domain.time.buildDivinationTime
import site.hanzhe.wuchang_liuyao.domain.time.currentGregorianDivinationTime
import java.util.Date

private const val TimeSelectionViewModelTag = "TimeSelectionViewModel"
private const val TimeSelectionLoadFailedMessage = "时间数据加载失败"
private const val TimeSelectionNotFoundMessage = "未找到对应的时间数据"

private sealed interface TimeSelectionResolutionResult {
    data class Success(
        val calendarSummary: CalendarSummary,
        val divinationTime: DivinationTime,
        val timeSelectionUiState: TimeSelectionUiState
    ) : TimeSelectionResolutionResult

    data object NotFound : TimeSelectionResolutionResult
    data class CalculationError(val throwable: Throwable) : TimeSelectionResolutionResult
}

internal class TimeSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val calendarRepository = CnCalendarRepository()

    private val _uiState = MutableStateFlow(TimeSelectionHostUiState())
    val uiState: StateFlow<TimeSelectionHostUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
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

    fun consumeTimeSelectionCloseRequest() {
        _uiState.update { currentState ->
            if (!currentState.shouldCloseTimeSelection) {
                currentState
            } else {
                currentState.copy(shouldCloseTimeSelection = false)
            }
        }
    }

    fun refreshCalendarSummary() {
        viewModelScope.launch {
            val currentDate = Date()
            val currentSolarDateTime = currentGregorianDivinationTime(currentDate).solarDateTime
            when (
                val result = calendarRepository.queryCalendarBySolar(
                    solarYear = currentSolarDateTime.year,
                    solarMonth = currentSolarDateTime.month,
                    solarDay = currentSolarDateTime.day
                )
            ) {
                is CalendarQueryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            calendarSummary = result.value.toCalendarSummary(
                                hour = currentSolarDateTime.hour,
                                minute = currentSolarDateTime.minute
                            )
                        )
                    }
                }

                CalendarQueryResult.NotFound -> showTransientMessage(TimeSelectionNotFoundMessage)
                is CalendarQueryResult.CalculationError -> {
                    Log.e(TimeSelectionViewModelTag, "刷新当前农历失败", result.throwable)
                    showTransientMessage(TimeSelectionLoadFailedMessage)
                }
            }
        }
    }

    fun openTimeSelection() {
        _uiState.update { currentState ->
            val confirmedTimeSelectionUiState = currentState.confirmedTimeSelectionUiState
                ?: return@update currentState
            currentState.copy(
                timeSelectionUiState = confirmedTimeSelectionUiState.copy(activePicker = null)
            )
        }
    }

    fun closeTimeSelection() {
        _uiState.update { it.copy(timeSelectionUiState = null) }
    }

    fun confirmTimeSelection() {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            val resolvedUiState = if (timeSelectionUiState.selectedType == DivinationTimeType.GANZHI) {
                if (!timeSelectionUiState.ganzhiSelectionState.isRequiredFieldsComplete) {
                    return@update currentState
                }
                timeSelectionUiState.copy(
                    ganzhiText = timeSelectionUiState.ganzhiSelectionState.toGanzhiText(),
                    activePicker = null
                )
            } else {
                timeSelectionUiState.copy(activePicker = null)
            }
            applyConfirmedTimeSelection(
                currentState = currentState,
                timeSelectionUiState = resolvedUiState,
                shouldCloseTimeSelection = false
            )
        }
    }

    fun selectTimeType(type: DivinationTimeType) {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            val updatedTimeSelectionUiState = timeSelectionUiState.copy(
                selectedType = type,
                activePicker = null
            )
            currentState.copy(
                timeSelectionUiState = updatedTimeSelectionUiState
            )
        }
    }

    fun applyDefaultTimeType(type: DivinationTimeType) {
        _uiState.update { currentState ->
            val confirmedTimeSelectionUiState = currentState.confirmedTimeSelectionUiState
                ?: return@update currentState
            if (
                confirmedTimeSelectionUiState.selectedType == type &&
                currentState.divinationTime.type == type
            ) {
                return@update currentState
            }
            val updatedTimeSelectionUiState = confirmedTimeSelectionUiState.copy(
                selectedType = type,
                ganzhiSelectionState = if (type == DivinationTimeType.GANZHI) {
                    buildGanzhiSelectionStateFromText(confirmedTimeSelectionUiState.ganzhiText)
                } else {
                    confirmedTimeSelectionUiState.ganzhiSelectionState
                },
                activePicker = null
            )
            applyConfirmedTimeSelection(
                currentState = currentState,
                timeSelectionUiState = updatedTimeSelectionUiState,
                shouldCloseTimeSelection = false
            )
        }
    }

    fun showTimePicker(pickerType: TimePickerType) {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            currentState.copy(
                timeSelectionUiState = timeSelectionUiState.copy(activePicker = pickerType)
            )
        }
    }

    fun dismissTimePicker() {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            currentState.copy(
                timeSelectionUiState = timeSelectionUiState.copy(activePicker = null)
            )
        }
    }

    fun useCurrentTimeForTimeSelection() {
        val currentState = _uiState.value.timeSelectionUiState ?: return
        viewModelScope.launch {
            val currentSolarDateTime = currentGregorianDivinationTime(Date()).solarDateTime
            when (
                val result = resolveTimeSelectionBySolar(
                    solarDateTime = currentSolarDateTime,
                    selectedType = currentState.selectedType,
                    currentState = currentState
                )
            ) {
                is TimeSelectionResolutionResult.Success -> {
                    val resolvedUiState = if (currentState.selectedType == DivinationTimeType.GANZHI) {
                        val ganzhiSelectionState = buildGanzhiSelectionStateFromText(result.timeSelectionUiState.ganzhiText)
                        result.timeSelectionUiState.copy(
                            ganzhiSelectionState = ganzhiSelectionState,
                            ganzhiText = ganzhiSelectionState.toGanzhiText()
                        )
                    } else {
                        result.timeSelectionUiState
                    }
                    _uiState.update { latestState ->
                        applyConfirmedTimeSelection(
                            currentState = latestState,
                            timeSelectionUiState = resolvedUiState.copy(activePicker = null),
                            shouldCloseTimeSelection = true
                        )
                    }
                }

                TimeSelectionResolutionResult.NotFound -> showTransientMessage(TimeSelectionNotFoundMessage)
                is TimeSelectionResolutionResult.CalculationError -> {
                    Log.e(TimeSelectionViewModelTag, "获取当前时间失败", result.throwable)
                    showTransientMessage(TimeSelectionLoadFailedMessage)
                }
            }
        }
    }

    fun selectGanzhiField(fieldType: GanzhiFieldType) {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            currentState.copy(
                timeSelectionUiState = timeSelectionUiState.copy(
                    ganzhiSelectionState = timeSelectionUiState.ganzhiSelectionState.activateField(fieldType),
                    activePicker = null
                )
            )
        }
    }

    fun selectGanzhiOption(value: String) {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            if (timeSelectionUiState.selectedType != DivinationTimeType.GANZHI) {
                return@update currentState
            }
            val currentSelection = timeSelectionUiState.ganzhiSelectionState
            val activeField = currentSelection.activeField
            if (value !in currentSelection.optionsFor(activeField)) {
                return@update currentState
            }
            val updatedSelection = currentSelection.select(activeField, value)
            currentState.copy(
                timeSelectionUiState = timeSelectionUiState.copy(
                    ganzhiText = updatedSelection.toGanzhiText(),
                    ganzhiSelectionState = updatedSelection
                )
            )
        }
    }

    fun dismissGanzhiFieldDelete() {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            if (timeSelectionUiState.selectedType != DivinationTimeType.GANZHI) {
                return@update currentState
            }
            currentState.copy(
                timeSelectionUiState = timeSelectionUiState.copy(
                    ganzhiSelectionState = timeSelectionUiState.ganzhiSelectionState.dismissDeleteField()
                )
            )
        }
    }

    fun clearGanzhiField(fieldType: GanzhiFieldType) {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            if (timeSelectionUiState.selectedType != DivinationTimeType.GANZHI) {
                return@update currentState
            }
            val updatedSelection = timeSelectionUiState.ganzhiSelectionState.clearField(fieldType)
            currentState.copy(
                timeSelectionUiState = timeSelectionUiState.copy(
                    ganzhiText = updatedSelection.toGanzhiText(),
                    ganzhiSelectionState = updatedSelection
                )
            )
        }
    }

    fun resetGanzhiSelection() {
        _uiState.update { currentState ->
            val timeSelectionUiState = currentState.timeSelectionUiState ?: return@update currentState
            if (timeSelectionUiState.selectedType != DivinationTimeType.GANZHI) {
                return@update currentState
            }
            val updatedSelection = timeSelectionUiState.ganzhiSelectionState.reset()
            currentState.copy(
                timeSelectionUiState = timeSelectionUiState.copy(
                    ganzhiText = "",
                    ganzhiSelectionState = updatedSelection
                )
            )
        }
    }

    fun selectTimePickerOption(optionId: String) {
        val timeSelectionUiState = _uiState.value.timeSelectionUiState ?: return
        when (timeSelectionUiState.activePicker) {
            TimePickerType.SolarYear -> updateTimeBySolar(
                timeSelectionUiState.solarDateTime.copy(year = optionId.toIntOrNull() ?: return)
            )

            TimePickerType.SolarMonth -> updateTimeBySolar(
                timeSelectionUiState.solarDateTime.copy(month = optionId.toIntOrNull() ?: return)
            )

            TimePickerType.SolarDay -> updateTimeBySolar(
                timeSelectionUiState.solarDateTime.copy(day = optionId.toIntOrNull() ?: return)
            )

            TimePickerType.LunarYear -> updateTimeByLunar(
                lunarDate = timeSelectionUiState.lunarDate.copy(year = optionId.toIntOrNull() ?: return),
                hour = timeSelectionUiState.solarDateTime.hour,
                minute = timeSelectionUiState.solarDateTime.minute
            )

            TimePickerType.LunarMonth -> {
                val monthParts = optionId.split(":")
                if (monthParts.size != 2) {
                    return
                }
                val month = monthParts[0].toIntOrNull() ?: return
                val isLeapMonth = monthParts[1] == "1"
                updateTimeByLunar(
                    lunarDate = timeSelectionUiState.lunarDate.copy(
                        month = month,
                        isLeapMonth = isLeapMonth
                    ),
                    hour = timeSelectionUiState.solarDateTime.hour,
                    minute = timeSelectionUiState.solarDateTime.minute
                )
            }

            TimePickerType.LunarDay -> updateTimeByLunar(
                lunarDate = timeSelectionUiState.lunarDate.copy(day = optionId.toIntOrNull() ?: return),
                hour = timeSelectionUiState.solarDateTime.hour,
                minute = timeSelectionUiState.solarDateTime.minute
            )

            TimePickerType.Hour -> updateTimeOnly(
                hour = optionId.toIntOrNull() ?: return,
                minute = timeSelectionUiState.solarDateTime.minute
            )

            TimePickerType.Minute -> updateTimeOnly(
                hour = timeSelectionUiState.solarDateTime.hour,
                minute = optionId.toIntOrNull() ?: return
            )

            null -> Unit
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val currentDivinationTime = currentGregorianDivinationTime(Date())
            when (
                val result = resolveTimeSelectionBySolar(
                    solarDateTime = currentDivinationTime.solarDateTime,
                    selectedType = currentDivinationTime.type,
                    currentState = null
                )
            ) {
                is TimeSelectionResolutionResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            calendarSummary = result.calendarSummary,
                            divinationTime = result.divinationTime,
                            confirmedTimeSelectionUiState = result.timeSelectionUiState
                        )
                    }
                }

                TimeSelectionResolutionResult.NotFound -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            divinationTime = currentDivinationTime,
                            startupFailure = StartupFailure.InvalidSystemTime
                        )
                    }
                }

                is TimeSelectionResolutionResult.CalculationError -> {
                    Log.e(TimeSelectionViewModelTag, "读取日历数据失败", result.throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            divinationTime = currentDivinationTime,
                            startupFailure = StartupFailure.CalendarDataLoadFailed
                        )
                    }
                }
            }
        }
    }

    private fun updateTimeBySolar(solarDateTime: SolarDateTime) {
        viewModelScope.launch {
            val currentState = _uiState.value.timeSelectionUiState ?: return@launch
            when (
                val result = resolveTimeSelectionBySolar(
                    solarDateTime = solarDateTime,
                    selectedType = currentState.selectedType,
                    currentState = currentState
                )
            ) {
                is TimeSelectionResolutionResult.Success -> applyTimeSelectionResult(result)
                TimeSelectionResolutionResult.NotFound -> showTransientMessage(TimeSelectionNotFoundMessage)
                is TimeSelectionResolutionResult.CalculationError -> {
                    Log.e(TimeSelectionViewModelTag, "更新公历时间失败", result.throwable)
                    showTransientMessage(TimeSelectionLoadFailedMessage)
                }
            }
        }
    }

    private fun updateTimeByLunar(
        lunarDate: LunarDate,
        hour: Int,
        minute: Int
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value.timeSelectionUiState ?: return@launch
            when (
                val result = resolveTimeSelectionByLunar(
                    lunarDate = lunarDate,
                    hour = hour,
                    minute = minute,
                    selectedType = currentState.selectedType,
                    currentState = currentState
                )
            ) {
                is TimeSelectionResolutionResult.Success -> applyTimeSelectionResult(result)
                TimeSelectionResolutionResult.NotFound -> showTransientMessage(TimeSelectionNotFoundMessage)
                is TimeSelectionResolutionResult.CalculationError -> {
                    Log.e(TimeSelectionViewModelTag, "更新农历时间失败", result.throwable)
                    showTransientMessage(TimeSelectionLoadFailedMessage)
                }
            }
        }
    }

    private fun updateTimeOnly(hour: Int, minute: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value.timeSelectionUiState ?: return@launch
            when (
                val result = resolveTimeSelectionBySolar(
                    solarDateTime = currentState.solarDateTime.copy(hour = hour, minute = minute),
                    selectedType = currentState.selectedType,
                    currentState = currentState
                )
            ) {
                is TimeSelectionResolutionResult.Success -> applyTimeSelectionResult(result)
                TimeSelectionResolutionResult.NotFound -> showTransientMessage(TimeSelectionNotFoundMessage)
                is TimeSelectionResolutionResult.CalculationError -> {
                    Log.e(TimeSelectionViewModelTag, "更新时间失败", result.throwable)
                    showTransientMessage(TimeSelectionLoadFailedMessage)
                }
            }
        }
    }

    private fun applyTimeSelectionResult(result: TimeSelectionResolutionResult.Success) {
        _uiState.update { currentState ->
            currentState.copy(
                timeSelectionUiState = result.timeSelectionUiState
            )
        }
    }

    private fun showTransientMessage(message: String) {
        _uiState.update { it.copy(transientMessage = message) }
    }

    private fun applyConfirmedTimeSelection(
        currentState: TimeSelectionHostUiState,
        timeSelectionUiState: TimeSelectionUiState,
        shouldCloseTimeSelection: Boolean
    ): TimeSelectionHostUiState {
        return currentState.copy(
            shouldCloseTimeSelection = shouldCloseTimeSelection,
            divinationTime = buildDivinationTime(
                type = timeSelectionUiState.selectedType,
                solarDateTime = timeSelectionUiState.solarDateTime,
                lunarDate = timeSelectionUiState.lunarDate,
                ganzhiText = timeSelectionUiState.ganzhiText
            ),
            confirmedTimeSelectionUiState = timeSelectionUiState,
            timeSelectionUiState = null
        )
    }

    private suspend fun resolveTimeSelectionBySolar(
        solarDateTime: SolarDateTime,
        selectedType: DivinationTimeType,
        currentState: TimeSelectionUiState?
    ): TimeSelectionResolutionResult {
        val solarYears = when (val result = loadSolarYears(currentState)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }
        val resolvedYear = solarDateTime.year.takeIf { it in solarYears } ?: solarYears.first()

        val solarMonths = when (val result = calendarRepository.querySolarMonths(resolvedYear)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }
        val resolvedMonth = solarDateTime.month.takeIf { it in solarMonths } ?: solarMonths.first()

        val solarDays = when (val result = calendarRepository.querySolarDays(resolvedYear, resolvedMonth)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }
        val resolvedDay = solarDateTime.day.takeIf { it in solarDays } ?: solarDays.first()

        val calendarRecord = when (
            val result = calendarRepository.queryCalendarBySolar(
                solarYear = resolvedYear,
                solarMonth = resolvedMonth,
                solarDay = resolvedDay
            )
        ) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }

        return buildTimeSelectionResolution(
            calendarRecord = calendarRecord,
            selectedType = selectedType,
            hour = solarDateTime.hour,
            minute = solarDateTime.minute,
            currentState = currentState,
            solarYears = solarYears
        )
    }

    private suspend fun resolveTimeSelectionByLunar(
        lunarDate: LunarDate,
        hour: Int,
        minute: Int,
        selectedType: DivinationTimeType,
        currentState: TimeSelectionUiState?
    ): TimeSelectionResolutionResult {
        val lunarYears = when (val result = loadLunarYears(currentState)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }
        val resolvedYear = lunarDate.year.takeIf { it in lunarYears } ?: lunarYears.first()

        val lunarMonths = when (val result = calendarRepository.queryLunarMonths(resolvedYear)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }
        val resolvedMonth = lunarMonths.firstOrNull {
            it.month == lunarDate.month && it.isLeapMonth == lunarDate.isLeapMonth
        } ?: lunarMonths.first()

        val lunarDays = when (
            val result = calendarRepository.queryLunarDays(
                year = resolvedYear,
                month = resolvedMonth.month,
                isLeapMonth = resolvedMonth.isLeapMonth
            )
        ) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }
        val resolvedDay = lunarDate.day.takeIf { it in lunarDays } ?: lunarDays.first()

        val calendarRecord = when (
            val result = calendarRepository.queryCalendarByLunar(
                lunarYear = resolvedYear,
                lunarMonth = resolvedMonth.month,
                lunarDay = resolvedDay,
                isLeapMonth = resolvedMonth.isLeapMonth
            )
        ) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }

        return buildTimeSelectionResolution(
            calendarRecord = calendarRecord,
            selectedType = selectedType,
            hour = hour,
            minute = minute,
            currentState = currentState,
            lunarYears = lunarYears
        )
    }

    private suspend fun buildTimeSelectionResolution(
        calendarRecord: CalendarRecord,
        selectedType: DivinationTimeType,
        hour: Int,
        minute: Int,
        currentState: TimeSelectionUiState?,
        solarYears: List<Int>? = null,
        lunarYears: List<Int>? = null
    ): TimeSelectionResolutionResult {
        val resolvedSolarYears = solarYears ?: when (val result = loadSolarYears(currentState)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }

        val resolvedLunarYears = lunarYears ?: when (val result = loadLunarYears(currentState)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }

        val solarMonths = when (val result = calendarRepository.querySolarMonths(calendarRecord.solarYear)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }

        val solarDays = when (
            val result = calendarRepository.querySolarDays(
                year = calendarRecord.solarYear,
                month = calendarRecord.solarMonth
            )
        ) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }

        val lunarMonths = when (val result = calendarRepository.queryLunarMonths(calendarRecord.lunarYear)) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }

        val lunarDays = when (
            val result = calendarRepository.queryLunarDays(
                year = calendarRecord.lunarYear,
                month = calendarRecord.lunarMonth,
                isLeapMonth = calendarRecord.isLeapMonth
            )
        ) {
            is CalendarQueryResult.Success -> result.value
            CalendarQueryResult.NotFound -> return TimeSelectionResolutionResult.NotFound
            is CalendarQueryResult.CalculationError -> {
                return TimeSelectionResolutionResult.CalculationError(result.throwable)
            }
        }

        val solarDateTime = SolarDateTime(
            year = calendarRecord.solarYear,
            month = calendarRecord.solarMonth,
            day = calendarRecord.solarDay,
            hour = hour,
            minute = minute
        )
        val lunarDate = calendarRecord.toLunarDate()
        val ganzhiText = buildGanzhiSummary(
            calendarRecord = calendarRecord,
            hour = hour,
            minute = minute
        )

        return TimeSelectionResolutionResult.Success(
            calendarSummary = calendarRecord.toCalendarSummary(
                hour = hour,
                minute = minute
            ),
            divinationTime = buildDivinationTime(
                type = selectedType,
                solarDateTime = solarDateTime,
                lunarDate = lunarDate,
                ganzhiText = ganzhiText
            ),
            timeSelectionUiState = TimeSelectionUiState(
                selectedType = selectedType,
                solarDateTime = solarDateTime,
                lunarDate = lunarDate,
                ganzhiText = ganzhiText,
                ganzhiSelectionState = currentState?.ganzhiSelectionState ?: GanzhiSelectionState(),
                solarYearOptions = resolvedSolarYears,
                solarMonthOptions = solarMonths,
                solarDayOptions = solarDays,
                lunarYearOptions = resolvedLunarYears,
                lunarMonthOptions = lunarMonths,
                lunarDayOptions = lunarDays,
                activePicker = null
            )
        )
    }

    private suspend fun loadSolarYears(
        currentState: TimeSelectionUiState?
    ): CalendarQueryResult<List<Int>> {
        val options = currentState?.solarYearOptions.orEmpty()
        return if (options.isNotEmpty()) {
            CalendarQueryResult.Success(options)
        } else {
            calendarRepository.querySolarYears()
        }
    }

    private suspend fun loadLunarYears(
        currentState: TimeSelectionUiState?
    ): CalendarQueryResult<List<Int>> {
        val options = currentState?.lunarYearOptions.orEmpty()
        return if (options.isNotEmpty()) {
            CalendarQueryResult.Success(options)
        } else {
            calendarRepository.queryLunarYears()
        }
    }
}
