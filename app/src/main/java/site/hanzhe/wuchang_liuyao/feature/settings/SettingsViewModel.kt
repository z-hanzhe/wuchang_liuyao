package site.hanzhe.wuchang_liuyao.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import site.hanzhe.wuchang_liuyao.data.history.DefaultHistoryGroupId
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryGroup
import site.hanzhe.wuchang_liuyao.data.history.DivinationHistoryRepository
import site.hanzhe.wuchang_liuyao.data.settings.AppFontScale
import site.hanzhe.wuchang_liuyao.data.settings.AppSettingsRepository
import site.hanzhe.wuchang_liuyao.data.settings.AutoSaveDivinationMode
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType
import site.hanzhe.wuchang_liuyao.feature.home.DivinationMethod

internal data class SettingsUiState(
    val showLunarInfo: Boolean = true,
    val fontScale: Float = AppFontScale.Default,
    val changeDayPillarAt23: Boolean = true,
    val defaultDivinationMethod: DivinationMethod = DivinationMethod.YAO_NAME,
    val defaultDivinationTimeType: DivinationTimeType = DivinationTimeType.GREGORIAN,
    val compactSixGod: Boolean = false,
    val showHeavenlyStem: Boolean = false,
    val showAllHiddenLines: Boolean = false,
    val compactSixRelative: Boolean = false,
    val showHuiTouShengKeHint: Boolean = false,
    val showHuiTouChongHeHint: Boolean = false,
    val showDayMonthChongHeHint: Boolean = false,
    val markBranchXunKong: Boolean = false,
    val clickHighlightHint: Boolean = false,
    val autoSaveDivinationMode: AutoSaveDivinationMode = AutoSaveDivinationMode.OFF,
    val autoSaveHistoryGroupId: String = DefaultHistoryGroupId,
    val historyGroups: List<DivinationHistoryGroup> = emptyList()
)

internal class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val appSettingsRepository = AppSettingsRepository(application.applicationContext)
    private val historyRepository = DivinationHistoryRepository(application.applicationContext)
    private val initialHistoryGroups = historyRepository.getAllGroups()
    private val initialAutoSaveGroupId = resolveAutoSaveHistoryGroupId(
        groupId = appSettingsRepository.getAutoSaveHistoryGroupId(),
        groups = initialHistoryGroups
    )

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            showLunarInfo = appSettingsRepository.getShowLunarInfo(),
            fontScale = appSettingsRepository.getAppFontScale(),
            changeDayPillarAt23 = appSettingsRepository.getChangeDayPillarAt23(),
            defaultDivinationMethod = resolveDefaultDivinationMethod(
                appSettingsRepository.getDefaultDivinationMethodName()
            ),
            defaultDivinationTimeType = resolveDefaultDivinationTimeType(
                appSettingsRepository.getDefaultDivinationTimeTypeName()
            ),
            compactSixGod = appSettingsRepository.getCompactSixGod(),
            showHeavenlyStem = appSettingsRepository.getShowHeavenlyStem(),
            showAllHiddenLines = appSettingsRepository.getShowAllHiddenLines(),
            compactSixRelative = appSettingsRepository.getCompactSixRelative(),
            showHuiTouShengKeHint = appSettingsRepository.getShowHuiTouShengKeHint(),
            showHuiTouChongHeHint = appSettingsRepository.getShowHuiTouChongHeHint(),
            showDayMonthChongHeHint = appSettingsRepository.getShowDayMonthChongHeHint(),
            markBranchXunKong = appSettingsRepository.getMarkBranchXunKong(),
            clickHighlightHint = appSettingsRepository.getClickHighlightHint(),
            autoSaveDivinationMode = resolveAutoSaveDivinationMode(
                appSettingsRepository.getAutoSaveDivinationModeName()
            ),
            autoSaveHistoryGroupId = initialAutoSaveGroupId,
            historyGroups = initialHistoryGroups
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setShowLunarInfo(showLunarInfo: Boolean) {
        appSettingsRepository.setShowLunarInfo(showLunarInfo)
        _uiState.update { it.copy(showLunarInfo = showLunarInfo) }
    }

    fun setAppFontScale(fontScale: Float) {
        val normalizedScale = AppFontScale.normalize(fontScale)
        appSettingsRepository.setAppFontScale(normalizedScale)
        _uiState.update { it.copy(fontScale = normalizedScale) }
    }

    fun setChangeDayPillarAt23(changeDayPillarAt23: Boolean) {
        appSettingsRepository.setChangeDayPillarAt23(changeDayPillarAt23)
        _uiState.update { it.copy(changeDayPillarAt23 = changeDayPillarAt23) }
    }

    fun setDefaultDivinationMethod(defaultDivinationMethod: DivinationMethod) {
        appSettingsRepository.setDefaultDivinationMethodName(defaultDivinationMethod.name)
        _uiState.update { it.copy(defaultDivinationMethod = defaultDivinationMethod) }
    }

    fun setDefaultDivinationTimeType(defaultDivinationTimeType: DivinationTimeType) {
        appSettingsRepository.setDefaultDivinationTimeTypeName(defaultDivinationTimeType.name)
        _uiState.update { it.copy(defaultDivinationTimeType = defaultDivinationTimeType) }
    }

    fun setCompactSixGod(compactSixGod: Boolean) {
        appSettingsRepository.setCompactSixGod(compactSixGod)
        _uiState.update { it.copy(compactSixGod = compactSixGod) }
    }

    fun setShowHeavenlyStem(showHeavenlyStem: Boolean) {
        appSettingsRepository.setShowHeavenlyStem(showHeavenlyStem)
        _uiState.update { it.copy(showHeavenlyStem = showHeavenlyStem) }
    }

    fun setShowAllHiddenLines(showAllHiddenLines: Boolean) {
        appSettingsRepository.setShowAllHiddenLines(showAllHiddenLines)
        _uiState.update { it.copy(showAllHiddenLines = showAllHiddenLines) }
    }

    fun setCompactSixRelative(compactSixRelative: Boolean) {
        appSettingsRepository.setCompactSixRelative(compactSixRelative)
        _uiState.update { it.copy(compactSixRelative = compactSixRelative) }
    }

    fun setShowHuiTouShengKeHint(showHuiTouShengKeHint: Boolean) {
        appSettingsRepository.setShowHuiTouShengKeHint(showHuiTouShengKeHint)
        _uiState.update { it.copy(showHuiTouShengKeHint = showHuiTouShengKeHint) }
    }

    fun setShowHuiTouChongHeHint(showHuiTouChongHeHint: Boolean) {
        appSettingsRepository.setShowHuiTouChongHeHint(showHuiTouChongHeHint)
        _uiState.update { it.copy(showHuiTouChongHeHint = showHuiTouChongHeHint) }
    }

    fun setShowDayMonthChongHeHint(showDayMonthChongHeHint: Boolean) {
        appSettingsRepository.setShowDayMonthChongHeHint(showDayMonthChongHeHint)
        _uiState.update { it.copy(showDayMonthChongHeHint = showDayMonthChongHeHint) }
    }

    fun setMarkBranchXunKong(markBranchXunKong: Boolean) {
        appSettingsRepository.setMarkBranchXunKong(markBranchXunKong)
        _uiState.update { it.copy(markBranchXunKong = markBranchXunKong) }
    }

    fun setClickHighlightHint(clickHighlightHint: Boolean) {
        appSettingsRepository.setClickHighlightHint(clickHighlightHint)
        _uiState.update { it.copy(clickHighlightHint = clickHighlightHint) }
    }

    fun setAutoSaveDivinationMode(autoSaveDivinationMode: AutoSaveDivinationMode) {
        appSettingsRepository.setAutoSaveDivinationModeName(autoSaveDivinationMode.name)
        _uiState.update { it.copy(autoSaveDivinationMode = autoSaveDivinationMode) }
    }

    fun setAutoSaveHistoryGroup(groupId: String) {
        val groups = _uiState.value.historyGroups
        val resolvedGroupId = groupId.takeIf { currentGroupId ->
            groups.any { group -> group.id == currentGroupId }
        } ?: DefaultHistoryGroupId
        appSettingsRepository.setAutoSaveHistoryGroupId(resolvedGroupId)
        _uiState.update { it.copy(autoSaveHistoryGroupId = resolvedGroupId) }
    }

    fun refreshAutoSaveHistoryGroups() {
        val groups = historyRepository.getAllGroups()
        val resolvedGroupId = resolveAutoSaveHistoryGroupId(
            groupId = appSettingsRepository.getAutoSaveHistoryGroupId(),
            groups = groups
        )
        _uiState.update {
            it.copy(
                autoSaveHistoryGroupId = resolvedGroupId,
                historyGroups = groups
            )
        }
    }

    private fun resolveDefaultDivinationMethod(methodName: String): DivinationMethod {
        return DivinationMethod.entries.firstOrNull { it.name == methodName }
            ?: DivinationMethod.YAO_NAME
    }

    private fun resolveDefaultDivinationTimeType(timeTypeName: String): DivinationTimeType {
        return DivinationTimeType.entries.firstOrNull { it.name == timeTypeName }
            ?: DivinationTimeType.GREGORIAN
    }

    private fun resolveAutoSaveDivinationMode(modeName: String): AutoSaveDivinationMode {
        return AutoSaveDivinationMode.entries.firstOrNull { it.name == modeName }
            ?: AutoSaveDivinationMode.OFF
    }

    private fun resolveAutoSaveHistoryGroupId(
        groupId: String,
        groups: List<DivinationHistoryGroup>
    ): String {
        val resolvedGroupId = groupId.takeIf { currentGroupId ->
            groups.any { group -> group.id == currentGroupId }
        } ?: DefaultHistoryGroupId
        if (resolvedGroupId != groupId) {
            appSettingsRepository.setAutoSaveHistoryGroupId(resolvedGroupId)
        }
        return resolvedGroupId
    }
}
