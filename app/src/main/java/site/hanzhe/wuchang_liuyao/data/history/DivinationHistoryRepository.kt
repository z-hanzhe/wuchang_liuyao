package site.hanzhe.wuchang_liuyao.data.history

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationDateInfo
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationLine
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationRequest
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationResult
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationSpirit
import site.hanzhe.wuchang_liuyao.domain.divination.DivinationTableRow
import site.hanzhe.wuchang_liuyao.domain.divination.GanzhiPillar
import site.hanzhe.wuchang_liuyao.domain.time.DivinationTimeType

private const val DivinationHistoryPreferencesName = "divination_history"
private const val DivinationHistoryEntriesKey = "entries"
private const val DivinationHistoryGroupsKey = "groups"
internal const val DefaultHistoryGroupId = "default"
internal const val DefaultHistoryGroupName = "默认分组"

internal data class DivinationHistoryGroup(
    val id: String,
    val name: String,
    val sortOrder: Int,
    val isSystem: Boolean = false
)

internal data class DivinationHistoryRecord(
    val id: String,
    val createdAtMillis: Long,
    val groupId: String = DefaultHistoryGroupId,
    val request: DivinationRequest,
    val result: DivinationResult,
    val currentSituation: String = "",
    val judgment: String = ""
)

internal class DivinationHistoryRepository(
    context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(
        DivinationHistoryPreferencesName,
        Context.MODE_PRIVATE
    )

    fun getAllGroups(): List<DivinationHistoryGroup> {
        val rawGroups = sharedPreferences.getString(DivinationHistoryGroupsKey, null).orEmpty()
        if (rawGroups.isBlank()) {
            return listOf(defaultHistoryGroup())
        }
        val groups = buildList {
            val groupsJson = JSONArray(rawGroups)
            for (index in 0 until groupsJson.length()) {
                add(groupsJson.getJSONObject(index).toHistoryGroup())
            }
        }
        return normalizeGroups(groups)
    }

    fun getAllRecords(): List<DivinationHistoryRecord> {
        val rawEntries = sharedPreferences.getString(DivinationHistoryEntriesKey, null).orEmpty()
        if (rawEntries.isBlank()) {
            return emptyList()
        }
        val records = buildList {
            val entries = JSONArray(rawEntries)
            for (index in 0 until entries.length()) {
                val recordJson = entries.getJSONObject(index)
                add(recordJson.toHistoryRecord())
            }
        }
        return records.sortedByDescending { it.createdAtMillis }
    }

    fun saveRecord(
        request: DivinationRequest,
        result: DivinationResult,
        groupId: String = DefaultHistoryGroupId
    ): DivinationHistoryRecord {
        val existingRecord = getAllRecords().firstOrNull { record ->
            record.request.isSameDivination(request)
        }
        if (existingRecord != null) {
            return existingRecord
        }
        val resolvedGroupId = groupId.takeIf { currentGroupId ->
            getAllGroups().any { group -> group.id == currentGroupId }
        } ?: DefaultHistoryGroupId
        val record = DivinationHistoryRecord(
            id = UUID.randomUUID().toString(),
            createdAtMillis = System.currentTimeMillis(),
            groupId = resolvedGroupId,
            request = request,
            result = result,
            currentSituation = "",
            judgment = ""
        )
        persistRecords(getAllRecords() + record)
        return record
    }

    fun updateRecord(
        recordId: String,
        question: String,
        currentSituation: String,
        judgment: String
    ): DivinationHistoryRecord {
        val records = getAllRecords()
        var updatedRecord: DivinationHistoryRecord? = null
        val updatedRecords = records.map { record ->
            if (record.id != recordId) {
                record
            } else {
                record.copy(
                    request = record.request.copy(question = question),
                    result = record.result.copy(question = question),
                    currentSituation = currentSituation,
                    judgment = judgment
                ).also { resolvedRecord ->
                    updatedRecord = resolvedRecord
                }
            }
        }
        val resolvedRecord = updatedRecord ?: error("未找到要更新的排盘记录")
        persistRecords(updatedRecords)
        return resolvedRecord
    }

    fun deleteRecords(recordIds: Set<String>) {
        if (recordIds.isEmpty()) {
            return
        }
        persistRecords(
            getAllRecords().filterNot { record -> record.id in recordIds }
        )
    }

    fun createGroup(name: String): DivinationHistoryGroup {
        val trimmedName = name.trim()
        validateGroupName(
            groups = getAllGroups(),
            groupId = null,
            name = trimmedName
        )
        val groups = getAllGroups()
        val group = DivinationHistoryGroup(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            sortOrder = groups.size
        )
        persistGroups(groups + group)
        return group
    }

    fun renameGroup(groupId: String, name: String): DivinationHistoryGroup {
        val trimmedName = name.trim()
        val groups = getAllGroups()
        val group = groups.firstOrNull { it.id == groupId }
            ?: error("未找到要重命名的分组")
        if (group.isSystem) {
            error("系统内置分组不能重命名")
        }
        validateGroupName(
            groups = groups,
            groupId = groupId,
            name = trimmedName
        )
        val updatedGroup = group.copy(name = trimmedName)
        persistGroups(
            groups.map { currentGroup ->
                if (currentGroup.id == groupId) updatedGroup else currentGroup
            }
        )
        return updatedGroup
    }

    fun moveRecordsToGroup(sourceGroupIds: Set<String>, targetGroupId: String) {
        if (sourceGroupIds.isEmpty()) {
            return
        }
        val groups = getAllGroups()
        val existingGroupIds = groups.map { it.id }.toSet()
        val movableSourceGroupIds = sourceGroupIds - targetGroupId
        if (!existingGroupIds.containsAll(sourceGroupIds)) {
            error("未找到来源分组")
        }
        if (groups.none { it.id == targetGroupId }) {
            error("未找到目标分组")
        }
        if (movableSourceGroupIds.isEmpty()) {
            return
        }
        persistRecords(
            getAllRecords().map { record ->
                if (record.groupId in movableSourceGroupIds) {
                    record.copy(groupId = targetGroupId)
                } else {
                    record
                }
            }
        )
    }

    fun moveRecords(recordIds: Set<String>, targetGroupId: String) {
        if (recordIds.isEmpty()) {
            return
        }
        if (getAllGroups().none { it.id == targetGroupId }) {
            error("未找到目标分组")
        }
        persistRecords(
            getAllRecords().map { record ->
                if (record.id in recordIds) {
                    record.copy(groupId = targetGroupId)
                } else {
                    record
                }
            }
        )
    }

    fun deleteGroups(groupIds: Set<String>) {
        val removableGroupIds = groupIds - DefaultHistoryGroupId
        if (removableGroupIds.isEmpty()) {
            return
        }
        persistGroups(
            getAllGroups().filterNot { group -> group.id in removableGroupIds }
        )
        persistRecords(
            getAllRecords().filterNot { record -> record.groupId in removableGroupIds }
        )
    }

    fun reorderGroups(orderedGroupIds: List<String>) {
        val groupsById = getAllGroups().associateBy { it.id }
        val orderedGroups = buildList {
            groupsById[DefaultHistoryGroupId]?.let { add(it) }
            orderedGroupIds
                .filter { groupId -> groupId != DefaultHistoryGroupId }
                .mapNotNull { groupId -> groupsById[groupId] }
                .forEach { group -> add(group) }
            groupsById.values
                .filterNot { group -> any { it.id == group.id } }
                .filterNot { group -> group.id == DefaultHistoryGroupId }
                .forEach { group -> add(group) }
        }
        persistGroups(orderedGroups)
    }

    private fun persistGroups(groups: List<DivinationHistoryGroup>) {
        val groupsJson = JSONArray().apply {
            normalizeGroups(groups).forEachIndexed { index, group ->
                put(group.copy(sortOrder = index).toJson())
            }
        }
        sharedPreferences.edit()
            .putString(DivinationHistoryGroupsKey, groupsJson.toString())
            .apply()
    }

    private fun persistRecords(records: List<DivinationHistoryRecord>) {
        val recordsJson = JSONArray().apply {
            records.sortedByDescending { it.createdAtMillis }.forEach { record ->
                put(record.toJson())
            }
        }
        sharedPreferences.edit()
            .putString(DivinationHistoryEntriesKey, recordsJson.toString())
            .apply()
    }
}

private fun DivinationRequest.isSameDivination(other: DivinationRequest): Boolean {
    return question == other.question &&
        dateInfo.isSameDivinationTime(other.dateInfo) &&
        linesTopDown == other.linesTopDown
}

private fun DivinationDateInfo.isSameDivinationTime(other: DivinationDateInfo): Boolean {
    return timeType == other.timeType &&
        solarText == other.solarText &&
        lunarText == other.lunarText &&
        ganzhiText == other.ganzhiText &&
        year == other.year &&
        month == other.month &&
        day == other.day &&
        hour == other.hour
}

private fun defaultHistoryGroup(): DivinationHistoryGroup {
    return DivinationHistoryGroup(
        id = DefaultHistoryGroupId,
        name = DefaultHistoryGroupName,
        sortOrder = 0,
        isSystem = true
    )
}

private fun normalizeGroups(groups: List<DivinationHistoryGroup>): List<DivinationHistoryGroup> {
    val defaultGroup = groups.firstOrNull { it.id == DefaultHistoryGroupId }
        ?.copy(
            name = DefaultHistoryGroupName,
            sortOrder = 0,
            isSystem = true
        )
        ?: defaultHistoryGroup()
    val customGroups = groups
        .filterNot { it.id == DefaultHistoryGroupId }
        .sortedWith(compareBy<DivinationHistoryGroup> { it.sortOrder }.thenBy { it.name })
        .mapIndexed { index, group ->
            group.copy(
                sortOrder = index + 1,
                isSystem = false
            )
        }
    return listOf(defaultGroup) + customGroups
}

private fun validateGroupName(
    groups: List<DivinationHistoryGroup>,
    groupId: String?,
    name: String
) {
    require(name.isNotBlank()) { "分组名称不能为空" }
    require(groups.none { group -> group.id != groupId && group.name == name }) {
        "分组名称不能重复"
    }
}

private fun DivinationHistoryGroup.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("name", name)
        put("sortOrder", sortOrder)
        put("isSystem", isSystem)
    }
}

private fun JSONObject.toHistoryGroup(): DivinationHistoryGroup {
    val id = getNullableString("id").orEmpty().ifBlank { UUID.randomUUID().toString() }
    val isDefault = id == DefaultHistoryGroupId
    return DivinationHistoryGroup(
        id = id,
        name = if (isDefault) DefaultHistoryGroupName else getString("name"),
        sortOrder = optInt("sortOrder", Int.MAX_VALUE),
        isSystem = isDefault || optBoolean("isSystem", false)
    )
}

private fun DivinationHistoryRecord.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("createdAtMillis", createdAtMillis)
        put("groupId", groupId)
        put("request", request.toJson())
        put("result", result.toJson())
        put("currentSituation", currentSituation)
        put("judgment", judgment)
    }
}

private fun JSONObject.toHistoryRecord(): DivinationHistoryRecord {
    return DivinationHistoryRecord(
        id = getString("id"),
        createdAtMillis = getLong("createdAtMillis"),
        groupId = optString("groupId", DefaultHistoryGroupId).ifBlank { DefaultHistoryGroupId },
        request = getJSONObject("request").toDivinationRequest(),
        result = getJSONObject("result").toDivinationResult(),
        currentSituation = getNullableString("currentSituation").orEmpty(),
        judgment = getNullableString("judgment").orEmpty()
    )
}

private fun DivinationRequest.toJson(): JSONObject {
    return JSONObject().apply {
        put("question", question)
        put("methodLabel", methodLabel)
        put("dateInfo", dateInfo.toJson())
        put(
            "linesTopDown",
            JSONArray().apply {
                linesTopDown.forEach { line -> put(line.code) }
            }
        )
    }
}

private fun JSONObject.toDivinationRequest(): DivinationRequest {
    val linesJson = getJSONArray("linesTopDown")
    return DivinationRequest(
        question = getString("question"),
        methodLabel = getString("methodLabel"),
        dateInfo = getJSONObject("dateInfo").toDivinationDateInfo(),
        linesTopDown = buildList {
            for (index in 0 until linesJson.length()) {
                add(linesJson.getInt(index).toDivinationLine())
            }
        }
    )
}

private fun DivinationResult.toJson(): JSONObject {
    return JSONObject().apply {
        put("question", question)
        put("methodLabel", methodLabel)
        put("dateInfo", dateInfo.toJson())
        put(
            "spirits",
            JSONArray().apply {
                spirits.forEach { spirit -> put(spirit.toJson()) }
            }
        )
        put("dayXunKong", dayXunKong)
        put("baseHexagramName", baseHexagramName)
        put("palaceName", palaceName)
        putNullableString("hexagramType", hexagramType)
        putNullableString("changedHexagramName", changedHexagramName)
        putNullableString("changedPalaceName", changedPalaceName)
        putNullableString("changedHexagramType", changedHexagramType)
        put(
            "rowsTopDown",
            JSONArray().apply {
                rowsTopDown.forEach { row -> put(row.toJson()) }
            }
        )
    }
}

private fun JSONObject.toDivinationResult(): DivinationResult {
    val spiritsJson = getJSONArray("spirits")
    val rowsJson = getJSONArray("rowsTopDown")
    return DivinationResult(
        question = getString("question"),
        methodLabel = getString("methodLabel"),
        dateInfo = getJSONObject("dateInfo").toDivinationDateInfo(),
        spirits = buildList {
            for (index in 0 until spiritsJson.length()) {
                add(spiritsJson.getJSONObject(index).toDivinationSpirit())
            }
        },
        dayXunKong = getString("dayXunKong"),
        baseHexagramName = getString("baseHexagramName"),
        palaceName = getString("palaceName"),
        hexagramType = getNullableString("hexagramType"),
        changedHexagramName = getNullableString("changedHexagramName"),
        changedPalaceName = getNullableString("changedPalaceName"),
        changedHexagramType = getNullableString("changedHexagramType"),
        rowsTopDown = buildList {
            for (index in 0 until rowsJson.length()) {
                add(rowsJson.getJSONObject(index).toDivinationTableRow())
            }
        }
    )
}

private fun DivinationDateInfo.toJson(): JSONObject {
    return JSONObject().apply {
        put("displayText", displayText)
        put("timeType", timeType.name)
        put("solarText", solarText)
        put("lunarText", lunarText)
        put("ganzhiText", ganzhiText)
        put("termText", termText)
        put("year", year.toJson())
        put("month", month.toJson())
        put("day", day.toJson())
        put("hour", hour.toJson())
    }
}

private fun JSONObject.toDivinationDateInfo(): DivinationDateInfo {
    return DivinationDateInfo(
        displayText = getString("displayText"),
        timeType = DivinationTimeType.valueOf(getString("timeType")),
        solarText = getString("solarText"),
        lunarText = getString("lunarText"),
        ganzhiText = getString("ganzhiText"),
        termText = optString("termText"),
        year = getJSONObject("year").toGanzhiPillar(),
        month = getJSONObject("month").toGanzhiPillar(),
        day = getJSONObject("day").toGanzhiPillar(),
        hour = getJSONObject("hour").toGanzhiPillar()
    )
}

private fun GanzhiPillar.toJson(): JSONObject {
    return JSONObject().apply {
        putNullableString("heavenlyStem", heavenlyStem)
        putNullableString("earthlyBranch", earthlyBranch)
    }
}

private fun JSONObject.toGanzhiPillar(): GanzhiPillar {
    return GanzhiPillar(
        heavenlyStem = getNullableString("heavenlyStem"),
        earthlyBranch = getNullableString("earthlyBranch")
    )
}

private fun DivinationSpirit.toJson(): JSONObject {
    return JSONObject().apply {
        put("name", name)
        put("value", value)
    }
}

private fun JSONObject.toDivinationSpirit(): DivinationSpirit {
    return DivinationSpirit(
        name = getString("name"),
        value = getString("value")
    )
}

private fun DivinationTableRow.toJson(): JSONObject {
    return JSONObject().apply {
        put("sixGod", sixGod)
        put("hiddenRelative", hiddenRelative)
        put("hiddenStem", hiddenStem)
        put("hiddenBranch", hiddenBranch)
        put("hiddenElement", hiddenElement)
        put("baseRelative", baseRelative)
        put("baseStem", baseStem)
        put("baseBranch", baseBranch)
        put("baseElement", baseElement)
        put("baseLineSymbol", baseLineSymbol)
        put("shiYingMark", shiYingMark)
        put("changingSymbol", changingSymbol)
        put("changedLineSymbol", changedLineSymbol)
        put("changedRelative", changedRelative)
        put("changedStem", changedStem)
        put("changedBranch", changedBranch)
        put("changedElement", changedElement)
    }
}

private fun JSONObject.toDivinationTableRow(): DivinationTableRow {
    return DivinationTableRow(
        sixGod = getString("sixGod"),
        hiddenRelative = getString("hiddenRelative"),
        hiddenStem = getString("hiddenStem"),
        hiddenBranch = getString("hiddenBranch"),
        hiddenElement = getString("hiddenElement"),
        baseRelative = getString("baseRelative"),
        baseStem = getString("baseStem"),
        baseBranch = getString("baseBranch"),
        baseElement = getString("baseElement"),
        baseLineSymbol = getString("baseLineSymbol"),
        shiYingMark = getString("shiYingMark"),
        changingSymbol = getString("changingSymbol"),
        changedLineSymbol = getString("changedLineSymbol"),
        changedRelative = getString("changedRelative"),
        changedStem = getString("changedStem"),
        changedBranch = getString("changedBranch"),
        changedElement = getString("changedElement")
    )
}

private fun Int.toDivinationLine(): DivinationLine {
    return DivinationLine.entries.firstOrNull { line -> line.code == this }
        ?: error("未知爻值编码: $this")
}

private fun JSONObject.putNullableString(key: String, value: String?) {
    put(key, value ?: JSONObject.NULL)
}

private fun JSONObject.getNullableString(key: String): String? {
    return if (isNull(key)) null else getString(key)
}
