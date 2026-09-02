package site.hanzhe.wuchang_liuyao.data.settings

import android.content.Context
import site.hanzhe.wuchang_liuyao.data.history.DefaultHistoryGroupId

private const val AppSettingsPreferencesName = "app_settings"
private const val ShowLunarInfoKey = "show_lunar_info"
private const val AppFontSizeKey = "app_font_size"
private const val CompactSixGodKey = "compact_six_god"
private const val ShowHeavenlyStemKey = "show_heavenly_stem"
private const val ShowAllHiddenLinesKey = "show_all_hidden_lines"
private const val CompactSixRelativeKey = "compact_six_relative"
private const val ChangeDayPillarAt23Key = "change_day_pillar_at_23"
private const val HuiTouShengKeHintKey = "hui_tou_sheng_ke_hint"
private const val HuiTouChongHeHintKey = "hui_tou_chong_he_hint"
private const val DayMonthChongHeHintKey = "day_month_chong_he_hint"
private const val MarkBranchXunKongKey = "mark_branch_xun_kong"
private const val ClickHighlightHintKey = "click_highlight_hint"
private const val DefaultDivinationMethodKey = "default_divination_method"
private const val DefaultDivinationTimeTypeKey = "default_divination_time_type"
private const val AutoSaveDivinationModeKey = "auto_save_divination_mode"
private const val AutoSaveHistoryGroupIdKey = "auto_save_history_group_id"

internal enum class AutoSaveDivinationMode {
    OFF,
    QUESTION_NOT_EMPTY,
    ALWAYS;

    fun shouldAutoSave(question: String): Boolean {
        return when (this) {
            OFF -> false
            QUESTION_NOT_EMPTY -> question.isNotBlank()
            ALWAYS -> true
        }
    }
}

internal class AppSettingsRepository(
    context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(
        AppSettingsPreferencesName,
        Context.MODE_PRIVATE
    )

    fun getShowLunarInfo(): Boolean {
        return sharedPreferences.getBoolean(ShowLunarInfoKey, true)
    }

    fun setShowLunarInfo(showLunarInfo: Boolean) {
        sharedPreferences.edit()
            .putBoolean(ShowLunarInfoKey, showLunarInfo)
            .apply()
    }

    fun getAppFontScale(): Float {
        val value = sharedPreferences.all[AppFontSizeKey]
        return AppFontScale.normalize(
            when (value) {
                is Float -> value
                is Double -> value.toFloat()
                is Int -> value.toFloat()
                is Long -> value.toFloat()
                is String -> AppFontScale.fromLegacyKey(value)
                else -> AppFontScale.Default
            }
        )
    }

    fun setAppFontScale(fontScale: Float) {
        sharedPreferences.edit()
            .putFloat(AppFontSizeKey, AppFontScale.normalize(fontScale))
            .apply()
    }

    fun getCompactSixGod(): Boolean {
        return sharedPreferences.getBoolean(CompactSixGodKey, false)
    }

    fun setCompactSixGod(compactSixGod: Boolean) {
        sharedPreferences.edit()
            .putBoolean(CompactSixGodKey, compactSixGod)
            .apply()
    }

    fun getShowHeavenlyStem(): Boolean {
        return sharedPreferences.getBoolean(ShowHeavenlyStemKey, false)
    }

    fun setShowHeavenlyStem(showHeavenlyStem: Boolean) {
        sharedPreferences.edit()
            .putBoolean(ShowHeavenlyStemKey, showHeavenlyStem)
            .apply()
    }

    fun getShowAllHiddenLines(): Boolean {
        return sharedPreferences.getBoolean(ShowAllHiddenLinesKey, false)
    }

    fun setShowAllHiddenLines(showAllHiddenLines: Boolean) {
        sharedPreferences.edit()
            .putBoolean(ShowAllHiddenLinesKey, showAllHiddenLines)
            .apply()
    }

    fun getCompactSixRelative(): Boolean {
        return sharedPreferences.getBoolean(CompactSixRelativeKey, false)
    }

    fun setCompactSixRelative(compactSixRelative: Boolean) {
        sharedPreferences.edit()
            .putBoolean(CompactSixRelativeKey, compactSixRelative)
            .apply()
    }

    fun getChangeDayPillarAt23(): Boolean {
        return sharedPreferences.getBoolean(ChangeDayPillarAt23Key, true)
    }

    fun setChangeDayPillarAt23(changeDayPillarAt23: Boolean) {
        sharedPreferences.edit()
            .putBoolean(ChangeDayPillarAt23Key, changeDayPillarAt23)
            .apply()
    }

    fun getShowHuiTouShengKeHint(): Boolean {
        return sharedPreferences.getBoolean(HuiTouShengKeHintKey, false)
    }

    fun setShowHuiTouShengKeHint(showHuiTouShengKeHint: Boolean) {
        sharedPreferences.edit()
            .putBoolean(HuiTouShengKeHintKey, showHuiTouShengKeHint)
            .apply()
    }

    fun getShowHuiTouChongHeHint(): Boolean {
        return sharedPreferences.getBoolean(HuiTouChongHeHintKey, false)
    }

    fun setShowHuiTouChongHeHint(showHuiTouChongHeHint: Boolean) {
        sharedPreferences.edit()
            .putBoolean(HuiTouChongHeHintKey, showHuiTouChongHeHint)
            .apply()
    }

    fun getShowDayMonthChongHeHint(): Boolean {
        return sharedPreferences.getBoolean(DayMonthChongHeHintKey, false)
    }

    fun setShowDayMonthChongHeHint(showDayMonthChongHeHint: Boolean) {
        sharedPreferences.edit()
            .putBoolean(DayMonthChongHeHintKey, showDayMonthChongHeHint)
            .apply()
    }

    fun getMarkBranchXunKong(): Boolean {
        return sharedPreferences.getBoolean(MarkBranchXunKongKey, false)
    }

    fun setMarkBranchXunKong(markBranchXunKong: Boolean) {
        sharedPreferences.edit()
            .putBoolean(MarkBranchXunKongKey, markBranchXunKong)
            .apply()
    }

    fun getClickHighlightHint(): Boolean {
        return sharedPreferences.getBoolean(ClickHighlightHintKey, false)
    }

    fun setClickHighlightHint(clickHighlightHint: Boolean) {
        sharedPreferences.edit()
            .putBoolean(ClickHighlightHintKey, clickHighlightHint)
            .apply()
    }

    fun getDefaultDivinationMethodName(): String {
        return sharedPreferences.getString(
            DefaultDivinationMethodKey,
            "YAO_NAME"
        ) ?: "YAO_NAME"
    }

    fun setDefaultDivinationMethodName(methodName: String) {
        sharedPreferences.edit()
            .putString(DefaultDivinationMethodKey, methodName)
            .apply()
    }

    fun getDefaultDivinationTimeTypeName(): String {
        return sharedPreferences.getString(
            DefaultDivinationTimeTypeKey,
            "GREGORIAN"
        ) ?: "GREGORIAN"
    }

    fun setDefaultDivinationTimeTypeName(timeTypeName: String) {
        sharedPreferences.edit()
            .putString(DefaultDivinationTimeTypeKey, timeTypeName)
            .apply()
    }

    fun getAutoSaveDivinationModeName(): String {
        return sharedPreferences.getString(
            AutoSaveDivinationModeKey,
            AutoSaveDivinationMode.QUESTION_NOT_EMPTY.name
        ) ?: AutoSaveDivinationMode.QUESTION_NOT_EMPTY.name
    }

    fun setAutoSaveDivinationModeName(modeName: String) {
        sharedPreferences.edit()
            .putString(AutoSaveDivinationModeKey, modeName)
            .apply()
    }

    fun getAutoSaveHistoryGroupId(): String {
        return sharedPreferences.getString(
            AutoSaveHistoryGroupIdKey,
            DefaultHistoryGroupId
        ) ?: DefaultHistoryGroupId
    }

    fun setAutoSaveHistoryGroupId(groupId: String) {
        sharedPreferences.edit()
            .putString(AutoSaveHistoryGroupIdKey, groupId)
            .apply()
    }
}
